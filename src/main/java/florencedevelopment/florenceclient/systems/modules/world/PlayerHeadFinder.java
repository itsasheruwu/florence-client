/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.modules.world;

import florencedevelopment.florenceclient.events.world.TickEvent;
import florencedevelopment.florenceclient.mixin.ChunkAccessor;
import florencedevelopment.florenceclient.settings.IntSetting;
import florencedevelopment.florenceclient.settings.Setting;
import florencedevelopment.florenceclient.settings.SettingGroup;
import florencedevelopment.florenceclient.systems.modules.Categories;
import florencedevelopment.florenceclient.systems.modules.Module;
import florencedevelopment.florenceclient.utils.world.ChunkIterator;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.util.Map;

public class PlayerHeadFinder extends Module {
    private static final int REFRESH_INTERVAL_TICKS = 20;
    private static final int REPORT_INTERVAL_TICKS = 20 * 30;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> horizontalRadius = sgGeneral.add(new IntSetting.Builder()
        .name("horizontal-radius")
        .description("How far to search around you horizontally.")
        .defaultValue(32)
        .min(1)
        .sliderRange(1, 128)
        .build()
    );

    private final Setting<Integer> verticalRadius = sgGeneral.add(new IntSetting.Builder()
        .name("vertical-radius")
        .description("How far to search above and below you.")
        .defaultValue(16)
        .min(1)
        .sliderRange(1, 64)
        .build()
    );

    private int ticksUntilRefresh;
    private int ticksUntilReport;
    private int nearbyHeads;
    private int lastPlayerChunkX;
    private int lastPlayerChunkZ;
    private int lastPlayerY;

    public PlayerHeadFinder() {
        super(Categories.World, "player-head-finder", "Counts nearby player heads and reports the total in chat every 30 seconds.");
    }

    @Override
    public void onActivate() {
        nearbyHeads = 0;
        ticksUntilRefresh = 0;
        ticksUntilReport = 0;
        lastPlayerChunkX = Integer.MIN_VALUE;
        lastPlayerChunkZ = Integer.MIN_VALUE;
        lastPlayerY = Integer.MIN_VALUE;
        refreshCount();
        reportCount();
    }

    @Override
    public void onDeactivate() {
        ticksUntilRefresh = 0;
        ticksUntilReport = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;

        if (shouldRefreshCount()) {
            refreshCount();
        } else {
            ticksUntilRefresh--;
        }

        if (--ticksUntilReport <= 0) {
            reportCount();
        }
    }

    @Override
    public String getInfoString() {
        return Integer.toString(nearbyHeads);
    }

    private boolean shouldRefreshCount() {
        int currentChunkX = mc.player.getChunkPos().x;
        int currentChunkZ = mc.player.getChunkPos().z;
        int currentY = mc.player.getBlockY();

        return ticksUntilRefresh <= 0
            || currentChunkX != lastPlayerChunkX
            || currentChunkZ != lastPlayerChunkZ
            || Math.abs(currentY - lastPlayerY) >= 2;
    }

    private void refreshCount() {
        int px = mc.player.getBlockX();
        int py = mc.player.getBlockY();
        int pz = mc.player.getBlockZ();

        int hRadius = horizontalRadius.get();
        int vRadius = verticalRadius.get();

        int minX = px - hRadius;
        int maxX = px + hRadius;
        int minZ = pz - hRadius;
        int maxZ = pz + hRadius;
        int minY = py - vRadius;
        int maxY = py + vRadius;
        int count = 0;

        ChunkIterator iterator = new ChunkIterator(false);
        while (iterator.hasNext()) {
            Chunk chunk = iterator.next();
            int chunkMinX = chunk.getPos().getStartX();
            int chunkMaxX = chunk.getPos().getEndX();
            int chunkMinZ = chunk.getPos().getStartZ();
            int chunkMaxZ = chunk.getPos().getEndZ();

            if (chunkMaxX < minX || chunkMinX > maxX || chunkMaxZ < minZ || chunkMinZ > maxZ) continue;

            Map<BlockPos, BlockEntity> blockEntities = ((ChunkAccessor) chunk).getBlockEntities();
            for (Map.Entry<BlockPos, BlockEntity> entry : blockEntities.entrySet()) {
                BlockPos pos = entry.getKey();

                if (pos.getX() < minX || pos.getX() > maxX) continue;
                if (pos.getY() < minY || pos.getY() > maxY) continue;
                if (pos.getZ() < minZ || pos.getZ() > maxZ) continue;

                var block = mc.world.getBlockState(pos).getBlock();
                if (block == Blocks.PLAYER_HEAD || block == Blocks.PLAYER_WALL_HEAD) count++;
            }
        }

        nearbyHeads = count;
        ticksUntilRefresh = REFRESH_INTERVAL_TICKS;
        lastPlayerChunkX = mc.player.getChunkPos().x;
        lastPlayerChunkZ = mc.player.getChunkPos().z;
        lastPlayerY = py;
    }

    private void reportCount() {
        ticksUntilReport = REPORT_INTERVAL_TICKS;
        int hRadius = horizontalRadius.get();
        int vRadius = verticalRadius.get();
        info(
            "Found (highlight)%d(default) player head%s within (highlight)%d(default)x(highlight)%d(default) blocks.",
            nearbyHeads,
            nearbyHeads == 1 ? "" : "s",
            hRadius,
            vRadius
        );
    }
}
