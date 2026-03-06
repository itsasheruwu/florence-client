/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixininterface;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector3d;

@SuppressWarnings("UnusedReturnValue")
public interface IVec3d {
    Vec3d florence$set(double x, double y, double z);

    default Vec3d florence$set(Vec3i vec) {
        return florence$set(vec.getX(), vec.getY(), vec.getZ());
    }

    default Vec3d florence$set(Vector3d vec) {
        return florence$set(vec.x, vec.y, vec.z);
    }

    default Vec3d florence$set(Vec3d pos) {
        return florence$set(pos.x, pos.y, pos.z);
    }

    Vec3d florence$setXZ(double x, double z);

    Vec3d florence$setY(double y);
}
