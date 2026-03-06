/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.screen.MountScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MountScreenHandler.class)
public interface MountScreenHandlerAccessor {
    @Accessor("mount")
    LivingEntity florence$getMount();
}
