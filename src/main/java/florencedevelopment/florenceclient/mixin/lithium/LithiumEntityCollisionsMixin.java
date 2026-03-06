/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin.lithium;

import florencedevelopment.florenceclient.systems.modules.Modules;
import florencedevelopment.florenceclient.systems.modules.world.Collisions;
import net.caffeinemc.mods.lithium.common.entity.LithiumEntityCollisions;
import net.minecraft.util.math.Box;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LithiumEntityCollisions.class)
public abstract class LithiumEntityCollisionsMixin {
    @Inject(method = "isWithinWorldBorder", at = @At("HEAD"), cancellable = true)
    private static void onIsWithinWorldBorder(WorldBorder border, Box box, CallbackInfoReturnable<Boolean> cir) {
        if (Modules.get().get(Collisions.class).ignoreBorder()) {
            cir.setReturnValue(true);
        }
    }
}
