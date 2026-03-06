/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.modules.world;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import florencedevelopment.florenceclient.FlorenceClient;
import florencedevelopment.florenceclient.events.render.Render3DEvent;
import florencedevelopment.florenceclient.events.world.ChunkDataEvent;
import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.widgets.WWidget;
import florencedevelopment.florenceclient.gui.widgets.containers.WHorizontalList;
import florencedevelopment.florenceclient.gui.widgets.containers.WTable;
import florencedevelopment.florenceclient.gui.widgets.containers.WVerticalList;
import florencedevelopment.florenceclient.gui.widgets.pressable.WButton;
import florencedevelopment.florenceclient.gui.widgets.pressable.WMinus;
import florencedevelopment.florenceclient.pathing.PathManagers;
import florencedevelopment.florenceclient.settings.*;
import florencedevelopment.florenceclient.systems.modules.Categories;
import florencedevelopment.florenceclient.systems.modules.Module;
import florencedevelopment.florenceclient.utils.Utils;
import florencedevelopment.florenceclient.utils.player.ChatUtils;
import florencedevelopment.florenceclient.utils.render.FlorenceToast;
import florencedevelopment.florenceclient.utils.render.RenderUtils;
import florencedevelopment.florenceclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

import java.io.*;
import java.util.*;

public class BaseFinder extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // --- Settings ---

    private final Setting<Integer> minimumTypes = sgGeneral.add(new IntSetting.Builder()
        .name("minimum-types")
        .description("How many different types of base blocks must be in a chunk. E.g. 2 means a bed AND a furnace, not just two beds.")
        .defaultValue(2)
        .min(1)
        .sliderMin(1)
        .sliderMax(6)
        .build()
    );

    private final Setting<Boolean> ignoreStructures = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-structures")
        .description("Skip chunks that look like vanilla structures (villages, igloos, witch huts, etc).")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> onlyInhabited = sgGeneral.add(new BoolSetting.Builder()
        .name("only-inhabited")
        .description("Only scan chunks that have been previously loaded by a player. Skips freshly generated chunks.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> minimumDistance = sgGeneral.add(new IntSetting.Builder()
        .name("minimum-distance")
        .description("Minimum distance from spawn to start scanning.")
        .defaultValue(0)
        .min(0)
        .sliderMax(10000)
        .build()
    );

    private final Setting<Boolean> notifications = sgGeneral.add(new BoolSetting.Builder()
        .name("notifications")
        .description("Notify when a base is found.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> tracers = sgGeneral.add(new BoolSetting.Builder()
        .name("tracers")
        .description("Draw tracer lines to found bases.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> tracerColor = sgGeneral.add(new ColorSetting.Builder()
        .name("tracer-color")
        .description("Color of tracers.")
        .defaultValue(new SettingColor(200, 50, 50, 255))
        .visible(tracers::get)
        .build()
    );

    // --- Data ---

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Map<ChunkPos, Vec3d> tracerPositions = new HashMap<>();
    public List<BaseChunk> bases = new ArrayList<>();

    public BaseFinder() {
        super(Categories.World, "base-finder", "Finds player bases by looking for chunks with multiple types of base blocks (beds, furnaces, crafting tables, etc).");
    }

    // --- Lifecycle ---

    @Override
    public void onActivate() {
        load();
    }

    @Override
    public void onDeactivate() {
        tracerPositions.clear();
    }

    // --- Chunk scanning ---

    @EventHandler
    private void onChunkData(ChunkDataEvent event) {
        WorldChunk worldChunk = event.chunk();

        // Skip freshly generated chunks that no player has ever been in
        if (onlyInhabited.get() && worldChunk.getInhabitedTime() <= 0) return;

        // Distance filter
        double cx = worldChunk.getPos().x * 16.0;
        double cz = worldChunk.getPos().z * 16.0;
        if (cx * cx + cz * cz < (double) minimumDistance.get() * minimumDistance.get()) return;

        // Scan the chunk for base indicator block categories
        boolean hasBed = false, hasCraftingTable = false, hasFurnace = false;
        boolean hasBrewingStand = false, hasAnvil = false, hasEnchantingTable = false;
        boolean hasStructureBlock = false;

        boolean checkStructures = ignoreStructures.get();
        BlockPos.Mutable blockPos = new BlockPos.Mutable();
        int bottomY = mc.world.getBottomY();
        int topY = bottomY + mc.world.getDimension().height();

        for (int x = worldChunk.getPos().getStartX(); x <= worldChunk.getPos().getEndX(); x++) {
            for (int z = worldChunk.getPos().getStartZ(); z <= worldChunk.getPos().getEndZ(); z++) {
                for (int y = bottomY; y < topY; y++) {
                    blockPos.set(x, y, z);
                    BlockState state = worldChunk.getBlockState(blockPos);
                    Block block = state.getBlock();

                    // Check for vanilla structure indicator blocks
                    if (checkStructures && !hasStructureBlock && isStructureBlock(block)) hasStructureBlock = true;

                    if (!hasBed && isBed(block)) hasBed = true;
                    else if (!hasCraftingTable && block == Blocks.CRAFTING_TABLE) hasCraftingTable = true;
                    else if (!hasFurnace && isFurnace(block)) hasFurnace = true;
                    else if (!hasBrewingStand && block == Blocks.BREWING_STAND) hasBrewingStand = true;
                    else if (!hasAnvil && isAnvil(block)) hasAnvil = true;
                    else if (!hasEnchantingTable && block == Blocks.ENCHANTING_TABLE) hasEnchantingTable = true;
                }
            }

            // Early exit if all 6 types found
            if (hasBed && hasCraftingTable && hasFurnace && hasBrewingStand && hasAnvil && hasEnchantingTable) break;
        }

        // Skip vanilla structures
        if (hasStructureBlock) return;

        int uniqueTypes = (hasBed ? 1 : 0) + (hasCraftingTable ? 1 : 0) + (hasFurnace ? 1 : 0)
            + (hasBrewingStand ? 1 : 0) + (hasAnvil ? 1 : 0) + (hasEnchantingTable ? 1 : 0);

        if (uniqueTypes < minimumTypes.get()) return;

        // Build a description of what was found
        List<String> found = new ArrayList<>();
        if (hasBed) found.add("Bed");
        if (hasCraftingTable) found.add("Crafting Table");
        if (hasFurnace) found.add("Furnace");
        if (hasBrewingStand) found.add("Brewing Stand");
        if (hasAnvil) found.add("Anvil");
        if (hasEnchantingTable) found.add("Enchanting Table");

        BaseChunk base = new BaseChunk(worldChunk.getPos(), uniqueTypes, String.join(", ", found));

        // Check if we already know about this chunk
        int i = bases.indexOf(base);
        boolean isNew = i < 0;

        if (isNew) bases.add(base);
        else bases.set(i, base);

        // Tracer
        if (tracers.get()) {
            double y = mc.player != null ? mc.player.getEyeY() : 0.0;
            tracerPositions.put(base.chunkPos, new Vec3d(base.x, y, base.z));
        }

        save();

        // Notify
        if (isNew && notifications.get()) {
            MutableText message = Text.literal("Base found at ")
                .formatted(Formatting.GRAY)
                .append(Text.literal("[" + base.x + ", " + base.z + "]").formatted(Formatting.WHITE, Formatting.UNDERLINE))
                .append(Text.literal(" (" + base.foundBlocks + ")").formatted(Formatting.GRAY));

            ChatUtils.sendMsg(message);

            FlorenceToast toast = new FlorenceToast.Builder(title).icon(Items.COMPASS).text("Base found!").build();
            mc.getToastManager().add(toast);
        }
    }

    // --- Block helpers ---

    private static boolean isBed(Block block) {
        return block == Blocks.WHITE_BED || block == Blocks.ORANGE_BED || block == Blocks.MAGENTA_BED
            || block == Blocks.LIGHT_BLUE_BED || block == Blocks.YELLOW_BED || block == Blocks.LIME_BED
            || block == Blocks.PINK_BED || block == Blocks.GRAY_BED || block == Blocks.LIGHT_GRAY_BED
            || block == Blocks.CYAN_BED || block == Blocks.PURPLE_BED || block == Blocks.BLUE_BED
            || block == Blocks.BROWN_BED || block == Blocks.GREEN_BED || block == Blocks.RED_BED
            || block == Blocks.BLACK_BED;
    }

    private static boolean isFurnace(Block block) {
        return block == Blocks.FURNACE || block == Blocks.BLAST_FURNACE || block == Blocks.SMOKER;
    }

    private static boolean isAnvil(Block block) {
        return block == Blocks.ANVIL || block == Blocks.CHIPPED_ANVIL || block == Blocks.DAMAGED_ANVIL;
    }

    private static boolean isStructureBlock(Block block) {
        return block == Blocks.BELL                // Villages
            || block == Blocks.HAY_BLOCK           // Villages
            || block == Blocks.COMPOSTER           // Villages
            || block == Blocks.LECTERN             // Villages
            || block == Blocks.BLUE_ICE            // Igloos
            || block == Blocks.SUSPICIOUS_GRAVEL   // Trail ruins
            || block == Blocks.SUSPICIOUS_SAND     // Desert temples, ocean ruins
            || block == Blocks.SPAWNER             // Dungeons, strongholds, trial chambers
            || block == Blocks.END_PORTAL_FRAME    // Strongholds
            || block == Blocks.TRIAL_SPAWNER;      // Trial chambers
    }

    // --- Rendering ---

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!tracers.get() || tracerPositions.isEmpty() || mc.player == null) return;

        for (Vec3d pos : tracerPositions.values()) {
            event.renderer.line(
                RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z,
                pos.x, mc.player.getEyeY(), pos.z,
                tracerColor.get()
            );
        }
    }

    // --- GUI ---

    @Override
    public WWidget getWidget(GuiTheme theme) {
        bases.sort(Comparator.comparingInt(b -> -b.types));

        WVerticalList list = theme.verticalList();

        WHorizontalList buttons = theme.horizontalList();
        WButton clear = buttons.add(theme.button("Clear All")).widget();
        list.add(buttons);

        WTable table = new WTable();
        if (!bases.isEmpty()) list.add(table);

        clear.action = () -> {
            bases.clear();
            tracerPositions.clear();
            table.clear();
            save();
        };

        fillTable(theme, table);
        return list;
    }

    private void fillTable(GuiTheme theme, WTable table) {
        for (BaseChunk base : bases) {
            table.add(theme.label(base.x + ", " + base.z)).padRight(10);
            table.add(theme.label(base.foundBlocks)).padRight(10);

            WButton gotoBtn = table.add(theme.button("Goto")).widget();
            gotoBtn.action = () -> PathManagers.get().moveTo(new BlockPos(base.x, 0, base.z), true);

            WMinus delete = table.add(theme.minus()).widget();
            delete.action = () -> {
                if (bases.remove(base)) {
                    tracerPositions.remove(base.chunkPos);
                    table.clear();
                    fillTable(theme, table);
                    save();
                }
            };

            table.row();
        }
    }

    // --- Persistence ---

    private void load() {
        File file = getJsonFile();
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            bases = GSON.fromJson(reader, new TypeToken<List<BaseChunk>>() {}.getType());
            if (bases == null) bases = new ArrayList<>();
            for (BaseChunk base : bases) base.calculatePos();
        } catch (Exception ignored) {
            bases = new ArrayList<>();
        }
    }

    private void save() {
        try {
            File file = getJsonFile();
            file.getParentFile().mkdirs();
            try (Writer writer = new FileWriter(file)) {
                GSON.toJson(bases, writer);
            }
        } catch (IOException e) {
            FlorenceClient.LOG.error("Error saving base finder data", e);
        }
    }

    private File getJsonFile() {
        return new File(new File(new File(FlorenceClient.FOLDER, "bases"), Utils.getFileWorldName()), "bases.json");
    }

    @Override
    public String getInfoString() {
        return String.valueOf(bases.size());
    }

    // --- Data class ---

    public static class BaseChunk {
        public ChunkPos chunkPos;
        public transient int x, z;
        public int types;
        public String foundBlocks;

        public BaseChunk(ChunkPos chunkPos, int types, String foundBlocks) {
            this.chunkPos = chunkPos;
            this.types = types;
            this.foundBlocks = foundBlocks;
            calculatePos();
        }

        public void calculatePos() {
            x = chunkPos.x * 16 + 8;
            z = chunkPos.z * 16 + 8;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BaseChunk that = (BaseChunk) o;
            return Objects.equals(chunkPos, that.chunkPos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(chunkPos);
        }
    }
}
