/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.modules.world;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import florencedevelopment.florenceclient.events.render.Render2DEvent;
import florencedevelopment.florenceclient.events.world.TickEvent;
import florencedevelopment.florenceclient.pathing.BaritoneUtils;
import florencedevelopment.florenceclient.renderer.Renderer2D;
import florencedevelopment.florenceclient.renderer.text.TextRenderer;
import florencedevelopment.florenceclient.settings.*;
import florencedevelopment.florenceclient.systems.modules.Categories;
import florencedevelopment.florenceclient.systems.modules.Module;
import florencedevelopment.florenceclient.utils.player.FindItemResult;
import florencedevelopment.florenceclient.utils.player.InvUtils;
import florencedevelopment.florenceclient.utils.player.PlayerUtils;
import florencedevelopment.florenceclient.utils.render.color.SettingColor;
import florencedevelopment.florenceclient.utils.schematic.BlockEntry;
import florencedevelopment.florenceclient.utils.schematic.SchematicData;
import florencedevelopment.florenceclient.utils.schematic.SchematicLoader;
import florencedevelopment.florenceclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.util.*;

public class StructureBuilder extends Module {

    // ── Phases & Modes ────────────────────────────────────────────────────────

    public enum Phase { IDLE, ANALYZING, GATHERING, BUILDING, PAUSED, DONE }
    public enum GatherMode { Automatic, Manual }

    // ── Setting Groups ────────────────────────────────────────────────────────

    private final SettingGroup sgGeneral   = settings.getDefaultGroup();
    private final SettingGroup sgGathering = settings.createGroup("Gathering");
    private final SettingGroup sgBuilding  = settings.createGroup("Building");
    private final SettingGroup sgHud       = settings.createGroup("HUD");

    // General

    private final Setting<String> schematicPath = sgGeneral.add(new StringSetting.Builder()
        .name("schematic-path")
        .description("Absolute path to a .litematic or .schem file.")
        .defaultValue("")
        .build()
    );

    private final Setting<Integer> originX = sgGeneral.add(new IntSetting.Builder()
        .name("origin-x")
        .description("World X coordinate for schematic [0,0,0].")
        .defaultValue(0)
        .noSlider()
        .build()
    );

    private final Setting<Integer> originY = sgGeneral.add(new IntSetting.Builder()
        .name("origin-y")
        .description("World Y coordinate for schematic [0,0,0].")
        .defaultValue(64)
        .noSlider()
        .build()
    );

    private final Setting<Integer> originZ = sgGeneral.add(new IntSetting.Builder()
        .name("origin-z")
        .description("World Z coordinate for schematic [0,0,0].")
        .defaultValue(0)
        .noSlider()
        .build()
    );

    private final Setting<Double> placeRadius = sgGeneral.add(new DoubleSetting.Builder()
        .name("place-radius")
        .description("Maximum reach distance for block placement.")
        .defaultValue(4.5)
        .min(0)
        .sliderMax(6)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotate toward the placement face.")
        .defaultValue(true)
        .build()
    );

    // Gathering

    private final Setting<GatherMode> gatherMode = sgGathering.add(new EnumSetting.Builder<GatherMode>()
        .name("gather-mode")
        .description("How to acquire missing materials.")
        .defaultValue(GatherMode.Manual)
        .build()
    );

    private final Setting<Integer> checkInterval = sgGathering.add(new IntSetting.Builder()
        .name("check-interval")
        .description("Ticks between re-checks of inventory during gathering.")
        .defaultValue(20)
        .range(5, 100)
        .sliderRange(5, 100)
        .visible(() -> gatherMode.get() == GatherMode.Automatic)
        .build()
    );

    // Building

    private final Setting<Integer> placeDelay = sgBuilding.add(new IntSetting.Builder()
        .name("place-delay")
        .description("Ticks between block placements.")
        .defaultValue(1)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );

    private final Setting<Boolean> skipExisting = sgBuilding.add(new BoolSetting.Builder()
        .name("skip-existing")
        .description("Skip positions where the correct block already exists.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> pauseOnMissing = sgBuilding.add(new BoolSetting.Builder()
        .name("pause-on-missing")
        .description("Pause instead of toggling off when a required block is not in the hotbar.")
        .defaultValue(true)
        .build()
    );

    // HUD

    private final Setting<Boolean> showHud = sgHud.add(new BoolSetting.Builder()
        .name("show-hud")
        .description("Show a material list overlay on screen.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> hudX = sgHud.add(new IntSetting.Builder()
        .name("hud-x")
        .description("HUD panel X position in pixels.")
        .defaultValue(4)
        .range(0, 4000)
        .noSlider()
        .build()
    );

    private final Setting<Integer> hudY = sgHud.add(new IntSetting.Builder()
        .name("hud-y")
        .description("HUD panel Y position in pixels.")
        .defaultValue(100)
        .range(0, 4000)
        .noSlider()
        .build()
    );

    private final Setting<Integer> hudMaxItems = sgHud.add(new IntSetting.Builder()
        .name("hud-max-items")
        .description("Maximum number of material rows to display.")
        .defaultValue(8)
        .range(1, 20)
        .sliderRange(1, 20)
        .build()
    );

    private final Setting<SettingColor> titleColor = sgHud.add(new ColorSetting.Builder()
        .name("title-color")
        .description("Color of the HUD title text.")
        .defaultValue(new SettingColor(255, 165, 0))
        .build()
    );

    private final Setting<SettingColor> needColor = sgHud.add(new ColorSetting.Builder()
        .name("need-color")
        .description("Color for blocks you still need.")
        .defaultValue(new SettingColor(255, 50, 50))
        .build()
    );

    private final Setting<SettingColor> haveColor = sgHud.add(new ColorSetting.Builder()
        .name("have-color")
        .description("Color for blocks you already have.")
        .defaultValue(new SettingColor(50, 255, 50))
        .build()
    );

    private final Setting<SettingColor> bgColor = sgHud.add(new ColorSetting.Builder()
        .name("bg-color")
        .description("HUD background panel color.")
        .defaultValue(new SettingColor(0, 0, 0, 140))
        .build()
    );

    // ── Runtime State ─────────────────────────────────────────────────────────

    private Phase phase = Phase.IDLE;
    private SchematicData schematic;
    private int buildIndex;
    private int placeTimer;
    private int gatherCheckTimer;

    private IBaritone baritone;
    private boolean baritoneWasMining;

    // materialDeficit: block -> how many more we need
    private final Map<Block, Integer> materialDeficit = new LinkedHashMap<>();
    // hudList: sorted entries for display (most-needed first)
    private final List<Map.Entry<Block, int[]>> hudList = new ArrayList<>();
    // hudList value: [needed_total, have_now]

    // ── Constructor ───────────────────────────────────────────────────────────

    public StructureBuilder() {
        super(Categories.World, "structure-builder", "Places a schematic block-by-block, gathering missing materials automatically or via a HUD list.");
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onActivate() {
        if (BaritoneUtils.IS_AVAILABLE) {
            baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        }

        String path = schematicPath.get().trim();
        if (path.isEmpty()) {
            error("Set schematic-path before activating.");
            toggle();
            return;
        }

        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            error("File not found: " + path);
            toggle();
            return;
        }

        // Litematica guard
        if (SchematicLoader.isLitematicWithoutMod(file)) {
            error("Litematica not installed – cannot load .litematic files without it.");
            toggle();
            return;
        }

        BlockPos origin = new BlockPos(originX.get(), originY.get(), originZ.get());

        schematic = SchematicLoader.load(file, origin);
        if (schematic == null) {
            error("Failed to load schematic: " + file.getName());
            toggle();
            return;
        }

        info("Loaded " + schematic.blocks.size() + " blocks from " + file.getName() + ".");

        buildIndex = 0;
        placeTimer = 0;
        gatherCheckTimer = 0;
        baritoneWasMining = false;
        phase = Phase.ANALYZING;
    }

    @Override
    public void onDeactivate() {
        stopBaritone();
        phase = Phase.IDLE;
        materialDeficit.clear();
        hudList.clear();
        schematic = null;
    }

    // ── Tick Handler ──────────────────────────────────────────────────────────

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!isActive() || schematic == null) return;

        switch (phase) {
            case ANALYZING -> tickAnalyzing();
            case GATHERING -> tickGathering();
            case BUILDING  -> tickBuilding();
            case PAUSED    -> tickPaused();
            case DONE      -> {
                info("Structure complete!");
                toggle();
            }
        }
    }

    // ── Phase Logic ───────────────────────────────────────────────────────────

    private void tickAnalyzing() {
        recomputeDeficit();
        rebuildHudList();

        if (materialDeficit.isEmpty()) {
            phase = Phase.BUILDING;
        } else {
            phase = Phase.GATHERING;
            if (gatherMode.get() == GatherMode.Automatic) {
                startBaritone();
            }
        }
    }

    private void tickGathering() {
        gatherCheckTimer++;
        int interval = (gatherMode.get() == GatherMode.Automatic)
            ? checkInterval.get()
            : 20;

        if (gatherCheckTimer >= interval) {
            gatherCheckTimer = 0;
            recomputeDeficit();
            rebuildHudList();

            if (materialDeficit.isEmpty()) {
                stopBaritone();
                phase = Phase.BUILDING;
            }
        }
    }

    private void tickBuilding() {
        if (placeTimer++ < placeDelay.get()) return;
        placeTimer = 0;

        while (buildIndex < schematic.blocks.size()) {
            BlockEntry entry = schematic.blocks.get(buildIndex);

            // Skip correct blocks
            if (skipExisting.get()) {
                BlockState existing = mc.world.getBlockState(entry.worldPos());
                if (existing.equals(entry.state())) {
                    buildIndex++;
                    continue;
                }
            }

            // Skip unplaceable or out-of-reach
            if (!BlockUtils.canPlace(entry.worldPos(), true)
                || !PlayerUtils.isWithin(entry.worldPos().toCenterPos(), placeRadius.get())) {
                buildIndex++;
                continue;
            }

            // Find required block in hotbar
            final Block target = entry.state().getBlock();
            FindItemResult item = InvUtils.findInHotbar(
                stack -> stack.getItem() instanceof BlockItem bi && bi.getBlock() == target
            );

            if (!item.found()) {
                if (pauseOnMissing.get()) {
                    phase = Phase.PAUSED;
                } else {
                    toggle();
                }
                return;
            }

            BlockUtils.place(entry.worldPos(), item, rotate.get(), 50, true);
            buildIndex++;
            return; // one block per delay cycle
        }

        phase = Phase.DONE;
    }

    private void tickPaused() {
        // Check if the next required block is now in the hotbar
        if (buildIndex < schematic.blocks.size()) {
            final Block target = schematic.blocks.get(buildIndex).state().getBlock();
            FindItemResult item = InvUtils.findInHotbar(
                stack -> stack.getItem() instanceof BlockItem bi && bi.getBlock() == target
            );
            if (item.found()) {
                phase = Phase.BUILDING;
            }
        } else {
            phase = Phase.DONE;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void recomputeDeficit() {
        materialDeficit.clear();

        for (Map.Entry<Block, Integer> req : schematic.totalRequired.entrySet()) {
            Block block = req.getKey();
            int required = req.getValue();

            int inInventory = countInInventory(block);
            int deficit = required - inInventory;
            if (deficit > 0) {
                materialDeficit.put(block, deficit);
            }
        }
    }

    private int countInInventory(Block block) {
        int count = 0;
        // Main inventory (slots 0-35) + offhand (slot 40)
        for (int i = 0; i <= 40; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem bi && bi.getBlock() == block) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private void rebuildHudList() {
        hudList.clear();

        for (Map.Entry<Block, Integer> req : schematic.totalRequired.entrySet()) {
            Block block = req.getKey();
            int required = req.getValue();
            int inInventory = countInInventory(block);
            // [required, have] stored in int[2]
            hudList.add(new AbstractMap.SimpleEntry<>(block, new int[]{required, inInventory}));
        }

        // Sort: blocks with deficit first (most deficit first), then sufficients
        hudList.sort((a, b) -> {
            int defA = a.getValue()[0] - a.getValue()[1];
            int defB = b.getValue()[0] - b.getValue()[1];
            return Integer.compare(defB, defA);
        });
    }

    private void startBaritone() {
        if (baritone == null || materialDeficit.isEmpty()) return;

        Block[] blocks = materialDeficit.keySet().toArray(new Block[0]);
        baritone.getPathingBehavior().cancelEverything();
        baritone.getMineProcess().mine(blocks);
        baritoneWasMining = true;
    }

    private void stopBaritone() {
        if (baritone != null && baritoneWasMining) {
            baritone.getPathingBehavior().cancelEverything();
            baritoneWasMining = false;
        }
    }

    // ── HUD Rendering ─────────────────────────────────────────────────────────

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (!showHud.get() || phase == Phase.IDLE || schematic == null) return;

        TextRenderer text = TextRenderer.get();

        // Measure dimensions (these work without begin/end context)
        double lineH = text.getHeight() + 2;
        double rx = hudX.get();
        double ry = hudY.get();

        String title = "Structure Builder [" + phase + "]";
        double maxWidth = text.getWidth(title);

        int displayCount = Math.min(hudList.size(), hudMaxItems.get());
        List<String[]> lines = new ArrayList<>(displayCount + 1);

        for (int i = 0; i < displayCount; i++) {
            Map.Entry<Block, int[]> entry = hudList.get(i);
            String blockName = Registries.BLOCK.getId(entry.getKey()).getPath();
            int required = entry.getValue()[0];
            int have = entry.getValue()[1];
            String label = blockName + ": " + have + "/" + required;
            lines.add(new String[]{label, have >= required ? "have" : "need"});
            maxWidth = Math.max(maxWidth, text.getWidth(label));
        }

        if (displayCount < hudList.size()) {
            String more = "... +" + (hudList.size() - displayCount) + " more";
            lines.add(new String[]{more, "have"});
            maxWidth = Math.max(maxWidth, text.getWidth(more));
        }

        double panelW = maxWidth + 6;
        double panelH = lineH * (1 + lines.size()) + 4;

        // Background quad
        Renderer2D.COLOR.begin();
        Renderer2D.COLOR.quad(rx - 2, ry - 2, panelW, panelH, bgColor.get());
        Renderer2D.COLOR.render();

        // Text
        text.begin();
        text.render(title, rx, ry, titleColor.get());
        double ty = ry + lineH;

        for (String[] line : lines) {
            boolean sufficient = "have".equals(line[1]);
            text.render(line[0], rx, ty, sufficient ? haveColor.get() : needColor.get());
            ty += lineH;
        }

        text.end();
    }

    // ── Info String ───────────────────────────────────────────────────────────

    @Override
    public String getInfoString() {
        return phase.name();
    }
}
