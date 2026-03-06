/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixininterface;

import net.minecraft.util.math.BlockPos;

public interface IBox {
    void florence$expand(double v);

    void florence$set(double x1, double y1, double z1, double x2, double y2, double z2);

    default void florence$set(BlockPos pos) {
        florence$set(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }
}
