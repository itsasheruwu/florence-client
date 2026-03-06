/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.utils.world;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import florencedevelopment.florenceclient.FlorenceClient;
import florencedevelopment.florenceclient.events.world.TickEvent;
import florencedevelopment.florenceclient.utils.PreInit;
import florencedevelopment.florenceclient.utils.Utils;
import florencedevelopment.florenceclient.utils.misc.Pool;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.BiConsumer;

import static florencedevelopment.florenceclient.FlorenceClient.mc;

public class BlockIterator {
    private static final Pool<Callback> callbackPool = new Pool<>(Callback::new);
    private static final List<Callback> callbacks = new ReferenceArrayList<>();

    private static final List<Runnable> afterCallbacks = new ReferenceArrayList<>();

    private static final BlockPos.Mutable blockPos = new BlockPos.Mutable();
    private static int hRadius, vRadius;

    private static boolean disableCurrent;

    private BlockIterator() {
    }

    @PreInit
    public static void init() {
        FlorenceClient.EVENT_BUS.subscribe(BlockIterator.class);
    }

    @EventHandler(priority = EventPriority.LOWEST - 1)
    private static void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;

        int px = mc.player.getBlockX();
        int py = mc.player.getBlockY();
        int pz = mc.player.getBlockZ();

        for (int x = px - hRadius; x <= px + hRadius; x++) {
            for (int z = pz - hRadius; z <= pz + hRadius; z++) {
                for (int y = Math.max(mc.world.getBottomY(), py - vRadius); y <= py + vRadius; y++) {
                    if (y > mc.world.getHeight()) break;

                    blockPos.set(x, y, z);
                    BlockState blockState = mc.world.getBlockState(blockPos);

                    int dx = Math.abs(x - px);
                    int dy = Math.abs(y - py);
                    int dz = Math.abs(z - pz);

                    callbacks.removeIf(callback -> {
                        if (dx <= callback.hRadius && dy <= callback.vRadius && dz <= callback.hRadius) {
                            disableCurrent = false;
                            callback.function.accept(blockPos, blockState);
                            return disableCurrent;
                        }
                        return false;
                    });
                }
            }
        }

        hRadius = 0;
        vRadius = 0;

        callbackPool.freeAll(callbacks);
        callbacks.clear();

        for (Runnable callback : afterCallbacks) callback.run();
        afterCallbacks.clear();
    }

    public static void register(int horizontalRadius, int verticalRadius, BiConsumer<BlockPos, BlockState> function) {
        hRadius = Math.max(hRadius, horizontalRadius);
        vRadius = Math.max(vRadius, verticalRadius);

        Callback callback = callbackPool.get();

        callback.function = function;
        callback.hRadius = horizontalRadius;
        callback.vRadius = verticalRadius;

        callbacks.add(callback);
    }

    public static void disableCurrent() {
        disableCurrent = true;
    }

    public static void after(Runnable callback) {
        afterCallbacks.add(callback);
    }

    private static class Callback {
        public BiConsumer<BlockPos, BlockState> function;
        public int hRadius, vRadius;
    }
}
