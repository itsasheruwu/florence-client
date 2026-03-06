/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixininterface;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public interface IRaycastContext {
    void florence$set(Vec3d start, Vec3d end, RaycastContext.ShapeType shapeType, RaycastContext.FluidHandling fluidHandling, Entity entity);
}
