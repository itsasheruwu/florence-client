/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.modules.render;

import florencedevelopment.florenceclient.events.entity.EntityAddedEvent;
import florencedevelopment.florenceclient.events.game.GameLeftEvent;
import florencedevelopment.florenceclient.events.render.Render2DEvent;
import florencedevelopment.florenceclient.events.render.Render3DEvent;
import florencedevelopment.florenceclient.events.world.TickEvent;
import florencedevelopment.florenceclient.renderer.text.TextRenderer;
import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.widgets.WWidget;
import florencedevelopment.florenceclient.gui.widgets.containers.WVerticalList;
import florencedevelopment.florenceclient.gui.widgets.pressable.WButton;
import florencedevelopment.florenceclient.renderer.ShapeMode;
import florencedevelopment.florenceclient.settings.*;
import florencedevelopment.florenceclient.systems.modules.Categories;
import florencedevelopment.florenceclient.systems.modules.Module;
import florencedevelopment.florenceclient.systems.modules.Modules;
import florencedevelopment.florenceclient.systems.modules.combat.KillAura;
import florencedevelopment.florenceclient.utils.Utils;
import florencedevelopment.florenceclient.utils.entity.EntityUtils;
import florencedevelopment.florenceclient.utils.player.FindItemResult;
import florencedevelopment.florenceclient.utils.player.InvUtils;
import florencedevelopment.florenceclient.utils.player.PlayerUtils;
import florencedevelopment.florenceclient.utils.player.Rotations;
import florencedevelopment.florenceclient.utils.player.SlotUtils;
import florencedevelopment.florenceclient.utils.misc.Keybind;
import florencedevelopment.florenceclient.utils.render.FlorenceToast;
import florencedevelopment.florenceclient.utils.render.NametagUtils;
import florencedevelopment.florenceclient.utils.render.RenderUtils;
import florencedevelopment.florenceclient.utils.render.color.Color;
import florencedevelopment.florenceclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TrialSpawnerBlock;
import net.minecraft.block.VaultBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.block.enums.TrialSpawnerState;
import net.minecraft.block.enums.VaultState;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.joml.Vector3d;

public class TrialHelper extends Module {
    private enum KeyDropAlertMode { Toast, Sound }
    private enum VaultBotKeySourceMode { HotbarOffhand, AutoMoveFromInventory, MainHandOnly }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColors = settings.createGroup("Colors");

    private final Setting<Boolean> highlightVaults = sgGeneral.add(new BoolSetting.Builder()
        .name("highlight-vaults")
        .description("Highlight unopened Trial Vaults.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> highlightSpawners = sgGeneral.add(new BoolSetting.Builder()
        .name("highlight-spawners")
        .description("Highlight Trial Spawners that are not yet completed.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> highlightKeys = sgGeneral.add(new BoolSetting.Builder()
        .name("highlight-keys")
        .description("Highlight dropped Trial Key items on the ground.")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<Integer> fillOpacity = sgGeneral.add(new IntSetting.Builder()
        .name("fill-opacity")
        .description("The opacity of the shape fill.")
        .visible(() -> shapeMode.get() != ShapeMode.Lines)
        .defaultValue(50)
        .range(0, 255)
        .sliderMax(255)
        .build()
    );

    private final Setting<Boolean> tracers = sgGeneral.add(new BoolSetting.Builder()
        .name("tracers")
        .description("Draw tracers to highlighted blocks and items.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> fadeDistance = sgGeneral.add(new DoubleSetting.Builder()
        .name("fade-distance")
        .description("The distance at which the color will fade.")
        .defaultValue(6)
        .min(0)
        .sliderMax(12)
        .build()
    );

    private final Setting<Boolean> showSpawnerState = sgGeneral.add(new BoolSetting.Builder()
        .name("show-spawner-state")
        .description("Show state text above Trial Spawners (Inactive, Waiting, Active, Cooldown, Complete).")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> spawnerStateScale = sgGeneral.add(new DoubleSetting.Builder()
        .name("spawner-state-scale")
        .description("Scale of the spawner state text.")
        .defaultValue(1)
        .min(0.5)
        .sliderMax(2)
        .visible(showSpawnerState::get)
        .build()
    );

    private final Setting<Boolean> keyDropAlert = sgGeneral.add(new BoolSetting.Builder()
        .name("key-drop-alert")
        .description("Alert when a Trial Key is dropped nearby.")
        .defaultValue(true)
        .build()
    );

    private final Setting<KeyDropAlertMode> keyDropAlertMode = sgGeneral.add(new EnumSetting.Builder<KeyDropAlertMode>()
        .name("key-drop-alert-mode")
        .description("How to alert: Toast shows a notification, Sound plays a chime.")
        .defaultValue(KeyDropAlertMode.Toast)
        .visible(keyDropAlert::get)
        .build()
    );

    private final Setting<Integer> keyDropAlertMaxDistance = sgGeneral.add(new IntSetting.Builder()
        .name("key-drop-alert-distance")
        .description("Only alert when the key drops within this distance.")
        .defaultValue(64)
        .min(8)
        .sliderMax(128)
        .visible(keyDropAlert::get)
        .build()
    );

    private final Setting<Boolean> vaultBot = sgGeneral.add(new BoolSetting.Builder()
        .name("vault-bot")
        .description("Automatically opens nearby vaults with matching keys.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> vaultBotRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("vault-bot-range")
        .description("Maximum range to open nearby vaults.")
        .defaultValue(4.5)
        .min(0)
        .sliderMax(6)
        .visible(vaultBot::get)
        .build()
    );

    private final Setting<Boolean> vaultBotRotate = sgGeneral.add(new BoolSetting.Builder()
        .name("vault-bot-rotate")
        .description("Rotates to the vault before opening it.")
        .defaultValue(true)
        .visible(vaultBot::get)
        .build()
    );

    private final Setting<VaultBotKeySourceMode> vaultBotKeySource = sgGeneral.add(new EnumSetting.Builder<VaultBotKeySourceMode>()
        .name("vault-bot-key-source")
        .description("Where to get keys from when opening vaults.")
        .defaultValue(VaultBotKeySourceMode.HotbarOffhand)
        .visible(vaultBot::get)
        .build()
    );

    private final Setting<Keybind> changePosOnPress = sgGeneral.add(new KeybindSetting.Builder()
        .name("change-position-on-press")
        .description("Teleport to the nearest unfinished Trial Spawner when the key is pressed.")
        .defaultValue(Keybind.none())
        .action(this::teleportToNearestUnfinishedSpawner)
        .build()
    );

    private final Setting<Integer> changePosMaxRange = sgGeneral.add(new IntSetting.Builder()
        .name("change-position-max-range")
        .description("Maximum block distance to consider spawners for teleport.")
        .defaultValue(64)
        .min(8)
        .sliderMax(128)
        .build()
    );

    private final Setting<SettingColor> vaultColor = sgColors.add(new ColorSetting.Builder()
        .name("vault-color")
        .description("Color for unopened Trial Vaults.")
        .defaultValue(new SettingColor(255, 200, 0, 255))
        .build()
    );

    private final Setting<SettingColor> ominousVaultColor = sgColors.add(new ColorSetting.Builder()
        .name("ominous-vault-color")
        .description("Color for unopened Ominous Trial Vaults.")
        .defaultValue(new SettingColor(180, 0, 255, 255))
        .build()
    );

    private final Setting<SettingColor> spawnerColor = sgColors.add(new ColorSetting.Builder()
        .name("spawner-color")
        .description("Color for incomplete Trial Spawners.")
        .defaultValue(new SettingColor(0, 255, 100, 255))
        .build()
    );

    private final Setting<SettingColor> ominousSpawnerColor = sgColors.add(new ColorSetting.Builder()
        .name("ominous-spawner-color")
        .description("Color for incomplete Ominous Trial Spawners.")
        .defaultValue(new SettingColor(180, 0, 255, 255))
        .build()
    );

    private final Setting<SettingColor> keyColor = sgColors.add(new ColorSetting.Builder()
        .name("key-color")
        .description("Color for dropped Trial Key items.")
        .defaultValue(new SettingColor(180, 100, 255, 255))
        .build()
    );

    private final Setting<SettingColor> ominousKeyColor = sgColors.add(new ColorSetting.Builder()
        .name("ominous-key-color")
        .description("Color for dropped Ominous Trial Key items.")
        .defaultValue(new SettingColor(255, 80, 80, 255))
        .build()
    );

    private final Set<BlockPos> openedVaults = new HashSet<>();
    private final Color lineColor = new Color(0, 0, 0, 0);
    private final Color sideColor = new Color(0, 0, 0, 0);
    private int count;

    public TrialHelper() {
        super(Categories.Render, "trial-helper", "Highlights unopened Trial Vaults, incomplete Trial Spawners, and dropped Trial Keys in Trial Chambers.");
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList list = theme.verticalList();
        WButton clear = list.add(theme.button("Clear Opened Vaults")).expandX().widget();
        clear.action = openedVaults::clear;
        return list;
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        openedVaults.clear();
    }

    @EventHandler
    private void onEntityAdded(EntityAddedEvent event) {
        if (!keyDropAlert.get()) return;
        if (!(event.entity instanceof ItemEntity itemEntity)) return;
        if (!isTrialKeyItem(itemEntity)) return;
        if (mc.player == null) return;

        int maxDist = keyDropAlertMaxDistance.get();
        if (PlayerUtils.distanceTo(itemEntity) > maxDist) return;

        boolean ominousKey = itemEntity.getStack().getItem() == Items.OMINOUS_TRIAL_KEY;
        String title = ominousKey ? "Ominous Trial Key" : "Trial Key";
        var icon = ominousKey ? Items.OMINOUS_TRIAL_KEY : Items.TRIAL_KEY;

        switch (keyDropAlertMode.get()) {
            case Toast -> {
                FlorenceToast toast = new FlorenceToast.Builder(title).icon(icon).text("Dropped nearby!").build();
                mc.getToastManager().add(toast);
            }
            case Sound -> mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 1.2f, 1));
        }
    }

    private boolean isUnopenedVault(BlockEntity blockEntity) {
        if (blockEntity.getType() != BlockEntityType.VAULT) return false;
        if (openedVaults.contains(blockEntity.getPos())) return false;
        BlockState state = mc.world.getBlockState(blockEntity.getPos());
        if (state.getBlock() != Blocks.VAULT) return false;
        if (!state.contains(VaultBlock.VAULT_STATE)) return true;
        VaultState vaultState = state.get(VaultBlock.VAULT_STATE);
        return vaultState == VaultState.INACTIVE || vaultState == VaultState.ACTIVE;
    }

    private boolean isOminousVault(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        return state.getBlock() == Blocks.VAULT && state.contains(VaultBlock.OMINOUS) && state.get(VaultBlock.OMINOUS);
    }

    private boolean isOminousSpawner(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        return state.getBlock() == Blocks.TRIAL_SPAWNER && state.contains(TrialSpawnerBlock.OMINOUS) && state.get(TrialSpawnerBlock.OMINOUS);
    }

    private boolean isIncompleteSpawner(BlockEntity blockEntity) {
        if (blockEntity.getType() != BlockEntityType.TRIAL_SPAWNER) return false;
        if (!(blockEntity instanceof TrialSpawnerBlockEntity trialSpawner)) return false;
        TrialSpawnerState state = trialSpawner.getSpawnerState();
        return state == TrialSpawnerState.INACTIVE
            || state == TrialSpawnerState.WAITING_FOR_PLAYERS
            || state == TrialSpawnerState.ACTIVE;
    }

    private boolean isTrialKeyItem(ItemEntity entity) {
        if (entity == null || entity.getStack().isEmpty()) return false;
        var item = entity.getStack().getItem();
        return item == Items.TRIAL_KEY || item == Items.OMINOUS_TRIAL_KEY;
    }

    private Item getRequiredKeyForVault(BlockPos vaultPos) {
        return isOminousVault(vaultPos) ? Items.OMINOUS_TRIAL_KEY : Items.TRIAL_KEY;
    }

    /** Uses the vault block entity's cached state so ominous type is reliable (client block state can lag). */
    private Item getRequiredKeyForVault(BlockEntity blockEntity) {
        if (blockEntity.getType() != BlockEntityType.VAULT) return Items.TRIAL_KEY;
        BlockState state = blockEntity.getCachedState();
        return state.getBlock() == Blocks.VAULT && state.contains(VaultBlock.OMINOUS) && state.get(VaultBlock.OMINOUS)
            ? Items.OMINOUS_TRIAL_KEY
            : Items.TRIAL_KEY;
    }

    private boolean hasActiveKillAuraTarget() {
        KillAura killAura = Modules.get().get(KillAura.class);
        if (killAura == null) return false;
        if (!killAura.isActive()) return false;
        if (killAura.attacking) return true;

        var target = killAura.getTarget();
        if (target == null || !target.isAlive()) return false;
        return !(target instanceof LivingEntity livingTarget) || !livingTarget.isDead();
    }

    private boolean canUseKeyForVault(Item requiredKey) {
        return switch (vaultBotKeySource.get()) {
            case MainHandOnly -> mc.player.getMainHandStack().getItem() == requiredKey;
            case HotbarOffhand -> InvUtils.findInHotbar(requiredKey).found();
            case AutoMoveFromInventory -> {
                if (InvUtils.findInHotbar(requiredKey).found()) {
                    yield true;
                }

                FindItemResult mainInvKey = InvUtils.find(stack -> stack.getItem() == requiredKey, SlotUtils.MAIN_START, SlotUtils.MAIN_END);
                FindItemResult emptyHotbarSlot = InvUtils.find(ItemStack::isEmpty, SlotUtils.HOTBAR_START, SlotUtils.HOTBAR_END);
                yield mainInvKey.found() && emptyHotbarSlot.found();
            }
        };
    }

    private FindItemResult resolveVaultKey(Item requiredKey) {
        return switch (vaultBotKeySource.get()) {
            case MainHandOnly -> {
                if (mc.player.getMainHandStack().getItem() == requiredKey) {
                    int selected = mc.player.getInventory().getSelectedSlot();
                    yield new FindItemResult(selected, mc.player.getInventory().getStack(selected).getCount());
                }
                yield new FindItemResult(-1, 0);
            }
            case HotbarOffhand -> InvUtils.findInHotbar(requiredKey);
            case AutoMoveFromInventory -> {
                FindItemResult hotbarOrOffhand = InvUtils.findInHotbar(requiredKey);
                if (hotbarOrOffhand.found()) yield hotbarOrOffhand;

                FindItemResult mainInvKey = InvUtils.find(stack -> stack.getItem() == requiredKey, SlotUtils.MAIN_START, SlotUtils.MAIN_END);
                if (!mainInvKey.found()) yield new FindItemResult(-1, 0);

                FindItemResult emptyHotbarSlot = InvUtils.find(ItemStack::isEmpty, SlotUtils.HOTBAR_START, SlotUtils.HOTBAR_END);
                if (!emptyHotbarSlot.found()) yield new FindItemResult(-1, 0);

                InvUtils.move().from(mainInvKey.slot()).toHotbar(emptyHotbarSlot.slot());
                yield new FindItemResult(emptyHotbarSlot.slot(), mc.player.getInventory().getStack(emptyHotbarSlot.slot()).getCount());
            }
        };
    }

    private BlockPos findBestVaultForBot() {
        double maxDistSq = vaultBotRange.get() * vaultBotRange.get();
        double bestDistSq = Double.MAX_VALUE;
        BlockPos bestPos = null;

        for (BlockEntity blockEntity : Utils.blockEntities()) {
            if (!isUnopenedVault(blockEntity)) continue;

            BlockPos pos = blockEntity.getPos();
            double distSq = PlayerUtils.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            if (distSq > maxDistSq) continue;

            Item requiredKey = getRequiredKeyForVault(blockEntity);
            if (!canUseKeyForVault(requiredKey)) continue;

            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                bestPos = pos.toImmutable();
            }
        }

        return bestPos;
    }

    private void interactVault(BlockPos pos, Hand hand, int previousSlot) {
        BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
        ActionResult result = mc.interactionManager.interactBlock(mc.player, hand, hitResult);

        if (result.isAccepted()) {
            mc.player.swingHand(hand);
        }

        if (mc.player.getInventory().getSelectedSlot() != previousSlot) {
            InvUtils.swap(previousSlot, false);
        }
    }

    private boolean tryOpenVault(BlockPos pos, Item requiredKey) {
        FindItemResult key = resolveVaultKey(requiredKey);
        if (!key.found()) return false;

        int previousSlot = mc.player.getInventory().getSelectedSlot();
        Hand hand = key.isOffhand() ? Hand.OFF_HAND : Hand.MAIN_HAND;

        if (hand == Hand.MAIN_HAND && key.slot() != previousSlot) {
            if (!InvUtils.swap(key.slot(), false)) return false;
        }

        Runnable interactAction = () -> interactVault(pos, hand, previousSlot);

        if (vaultBotRotate.get()) {
            Rotations.rotate(Rotations.getYaw(pos), Rotations.getPitch(pos), -100, interactAction);
        } else {
            interactAction.run();
        }

        return true;
    }

    /** Block at pos has no collision (player can pass through). */
    private boolean isPassable(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) return true;
        if (!state.getFluidState().isEmpty()) return false;
        return state.getCollisionShape(world, pos).isEmpty();
    }

    /** Block at pos has collision (can stand on it). */
    private boolean isSolid(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) return false;
        if (!state.getFluidState().isEmpty()) return false;
        return !state.getCollisionShape(world, pos).isEmpty();
    }

    /** Player can stand here: solid below, passable at feet and head. */
    private boolean isStandable(World world, BlockPos pos) {
        return isSolid(world, pos.down()) && isPassable(world, pos) && isPassable(world, pos.up());
    }

    /** BFS from start (block containing feet) to goal (block we want feet in). Returns waypoints as Vec3d (center, feet Y). */
    private List<Vec3d> findPathToBlock(BlockPos start, BlockPos goal, int maxNodes) {
        World world = mc.world;
        if (world == null) return List.of();

        Queue<BlockPos> queue = new ArrayDeque<>();
        Map<BlockPos, BlockPos> parent = new HashMap<>();
        queue.add(start);
        parent.put(start, start);
        int visited = 0;

        while (!queue.isEmpty() && visited < maxNodes) {
            BlockPos cur = queue.poll();
            visited++;
            if (cur.equals(goal)) {
                List<Vec3d> path = new ArrayList<>();
                BlockPos node = goal;
                while (node != null && !node.equals(parent.get(node))) {
                    path.add(0, new Vec3d(node.getX() + 0.5, node.getY() + 1, node.getZ() + 0.5));
                    node = parent.get(node);
                }
                path.add(0, new Vec3d(start.getX() + 0.5, start.getY() + 1, start.getZ() + 0.5));
                return path;
            }
            for (Direction dir : Direction.values()) {
                BlockPos next = cur.offset(dir);
                if (parent.containsKey(next)) continue;
                if (!isStandable(world, next)) continue;
                parent.put(next, cur);
                queue.add(next);
            }
        }
        return List.of();
    }

    private void teleportToNearestUnfinishedSpawner() {
        if (!Utils.canUpdate() || mc.player == null || mc.world == null) return;

        int maxRange = changePosMaxRange.get();
        double maxDistSq = (double) maxRange * maxRange;
        BlockPos nearest = null;
        double bestDistSq = Double.MAX_VALUE;

        for (BlockEntity blockEntity : Utils.blockEntities()) {
            if (!isIncompleteSpawner(blockEntity)) continue;

            BlockPos pos = blockEntity.getPos();
            double distSq = PlayerUtils.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            if (distSq > maxDistSq) continue;
            if (distSq >= bestDistSq) continue;

            bestDistSq = distSq;
            nearest = pos.toImmutable();
        }

        if (nearest == null) {
            mc.getToastManager().add(new FlorenceToast.Builder("Trial Helper").icon(Items.TRIAL_SPAWNER).text("No unfinished spawner in range.").build());
            return;
        }

        Vec3d targetPos = new Vec3d(nearest.getX() + 0.5, nearest.getY() + 1, nearest.getZ() + 0.5);
        BlockPos startBlock = mc.player.getBlockPos();
        BlockPos goalBlock = nearest.up(1);

        List<Vec3d> path = findPathToBlock(startBlock, goalBlock, 500);
        if (path.isEmpty() || path.size() < 2) {
            info("No path was found.");
            return;
        }
        if (!path.get(path.size() - 1).equals(targetPos)) {
            path = new ArrayList<>(path);
            path.set(path.size() - 1, targetPos);
        }

        for (Vec3d waypoint : path) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(waypoint.x, waypoint.y, waypoint.z, true, true));
        }
        mc.player.setPosition(targetPos);
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onTick(TickEvent.Pre event) {
        if (!vaultBot.get()) return;
        if (!Utils.canUpdate()) return;
        if (hasActiveKillAuraTarget()) return;

        BlockPos vaultPos = findBestVaultForBot();
        if (vaultPos == null) return;

        Item requiredKey = getRequiredKeyForVault(vaultPos);
        tryOpenVault(vaultPos, requiredKey);
    }

    private static String getSpawnerStateString(TrialSpawnerState state) {
        return switch (state) {
            case INACTIVE -> "Inactive";
            case WAITING_FOR_PLAYERS -> "Waiting";
            case ACTIVE -> "Active";
            case WAITING_FOR_REWARD_EJECTION -> "Ejecting soon";
            case EJECTING_REWARD -> "Ejecting";
            case COOLDOWN -> "Cooldown";
        };
    }

    /** Applies distance fade to line and side colors. Returns false if alpha is too low to render. */
    private boolean applyFade(double distSq, Color line, Color side) {
        double fadeSq = fadeDistance.get() * fadeDistance.get();
        double a = 1;
        if (distSq <= fadeSq) a = Math.sqrt(distSq) / fadeDistance.get();
        if (a < 0.075) return false;
        line.a = (int) (line.a * a);
        side.a = (int) (side.a * a);
        return true;
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (!Utils.canUpdate() || !showSpawnerState.get()) return;

        TextRenderer text = TextRenderer.get();
        double scale = spawnerStateScale.get();

        for (BlockEntity blockEntity : Utils.blockEntities()) {
            if (blockEntity.getType() != BlockEntityType.TRIAL_SPAWNER) continue;
            if (!EntityUtils.isInRenderDistance(blockEntity)) continue;
            if (!(blockEntity instanceof TrialSpawnerBlockEntity trialSpawner)) continue;

            BlockPos pos = blockEntity.getPos();
            TrialSpawnerState state = trialSpawner.getSpawnerState();
            String stateString = getSpawnerStateString(state);

            Vector3d pos3d = new Vector3d(pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5);
            if (!NametagUtils.to2D(pos3d, scale)) continue;

            boolean ominous = isOminousSpawner(pos);
            Color textColor = ominous ? ominousSpawnerColor.get() : spawnerColor.get();

            NametagUtils.begin(pos3d);
            text.begin();
            text.render(stateString, -text.getWidth(stateString, true) / 2, -text.getHeight(true), textColor, true);
            text.end();
            NametagUtils.end();
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!Utils.canUpdate()) return;
        count = 0;

        double centerX = RenderUtils.center.x;
        double centerY = RenderUtils.center.y;
        double centerZ = RenderUtils.center.z;

        for (BlockEntity blockEntity : Utils.blockEntities()) {
            if (!EntityUtils.isInRenderDistance(blockEntity)) continue;

            BlockPos pos = blockEntity.getPos();
            double distSq = PlayerUtils.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

            // When anyone (including other players) opens a vault, state becomes UNLOCKING then EJECTING; remember it so we stop highlighting
            if (blockEntity.getType() == BlockEntityType.VAULT && mc.world.getBlockState(pos).getBlock() == Blocks.VAULT) {
                BlockState state = mc.world.getBlockState(pos);
                if (state.contains(VaultBlock.VAULT_STATE)) {
                    VaultState vaultState = state.get(VaultBlock.VAULT_STATE);
                    if (vaultState == VaultState.UNLOCKING || vaultState == VaultState.EJECTING) {
                        openedVaults.add(pos.toImmutable());
                    }
                }
            }

            if (highlightVaults.get() && isUnopenedVault(blockEntity)) {
                boolean ominous = isOminousVault(pos);
                lineColor.set(ominous ? ominousVaultColor.get() : vaultColor.get());
                sideColor.set(ominous ? ominousVaultColor.get() : vaultColor.get());
                sideColor.a = shapeMode.get() != ShapeMode.Lines ? fillOpacity.get() : sideColor.a;
                if (!applyFade(distSq, lineColor, sideColor)) continue;
                if (tracers.get()) {
                    event.renderer.line(centerX, centerY, centerZ, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, lineColor);
                }
                event.renderer.box(pos, sideColor, lineColor, shapeMode.get(), 0);
                lineColor.a = (ominous ? ominousVaultColor.get() : vaultColor.get()).a;
                sideColor.a = lineColor.a;
                count++;
            } else if (highlightSpawners.get() && isIncompleteSpawner(blockEntity)) {
                boolean ominous = isOminousSpawner(pos);
                lineColor.set(ominous ? ominousSpawnerColor.get() : spawnerColor.get());
                sideColor.set(ominous ? ominousSpawnerColor.get() : spawnerColor.get());
                sideColor.a = shapeMode.get() != ShapeMode.Lines ? fillOpacity.get() : sideColor.a;
                if (!applyFade(distSq, lineColor, sideColor)) continue;
                if (tracers.get()) {
                    event.renderer.line(centerX, centerY, centerZ, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, lineColor);
                }
                event.renderer.box(pos, sideColor, lineColor, shapeMode.get(), 0);
                lineColor.a = (ominous ? ominousSpawnerColor.get() : spawnerColor.get()).a;
                sideColor.a = lineColor.a;
                count++;
            }
        }

        if (highlightKeys.get()) {
            for (var entity : mc.world.getEntities()) {
                if (!(entity instanceof ItemEntity itemEntity)) continue;
                if (!EntityUtils.isInRenderDistance(entity)) continue;
                if (!isTrialKeyItem(itemEntity)) continue;

                boolean ominousKey = itemEntity.getStack().getItem() == Items.OMINOUS_TRIAL_KEY;
                double x = MathHelper.lerp(event.tickDelta, entity.lastRenderX, entity.getX());
                double y = MathHelper.lerp(event.tickDelta, entity.lastRenderY, entity.getY());
                double z = MathHelper.lerp(event.tickDelta, entity.lastRenderZ, entity.getZ());
                Box box = entity.getBoundingBox();
                double dx = x - entity.getX();
                double dy = y - entity.getY();
                double dz = z - entity.getZ();

                lineColor.set(ominousKey ? ominousKeyColor.get() : keyColor.get());
                sideColor.set(ominousKey ? ominousKeyColor.get() : keyColor.get());
                sideColor.a = shapeMode.get() != ShapeMode.Lines ? fillOpacity.get() : sideColor.a;

                double distSq = PlayerUtils.squaredDistanceTo(x, y, z);
                if (!applyFade(distSq, lineColor, sideColor)) continue;

                if (tracers.get()) {
                    event.renderer.line(centerX, centerY, centerZ, x, y, z, lineColor);
                }
                event.renderer.box(
                    box.minX + dx, box.minY + dy, box.minZ + dz,
                    box.maxX + dx, box.maxY + dy, box.maxZ + dz,
                    sideColor, lineColor, shapeMode.get(), 0
                );
                lineColor.a = (ominousKey ? ominousKeyColor.get() : keyColor.get()).a;
                sideColor.a = lineColor.a;
                count++;
            }
        }
    }

    @Override
    public String getInfoString() {
        return Integer.toString(count);
    }
}
