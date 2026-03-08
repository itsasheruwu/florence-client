/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import florencedevelopment.florenceclient.systems.modules.Modules;
import florencedevelopment.florenceclient.systems.modules.movement.NoSlow;
import florencedevelopment.florenceclient.systems.modules.movement.speed.Speed;
import florencedevelopment.florenceclient.systems.modules.movement.speed.SpeedModes;
import net.minecraft.block.CobwebBlock;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static florencedevelopment.florenceclient.FlorenceClient.mc;

@Mixin(CobwebBlock.class)
public abstract class CobwebBlockMixin {
    @Dynamic("Explicit 1.21.9 Support")
    @Inject(method = {
        "onEntityCollision", // 1.21.10
        "onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/EntityCollisionHandler;)V", // 1.21.9 yarn
        "method_9548(Lnet/minecraft/class_2680;Lnet/minecraft/class_1937;Lnet/minecraft/class_2338;Lnet/minecraft/class_1297;Lnet/minecraft/class_10774;)V" // 1.21.9 intermediary
    }, at = @At("HEAD"), cancellable = true)
    private void onEntityCollision(CallbackInfo ci, @Local(argsOnly = true) Entity entity) {
        if (entity != mc.player) return;

        Speed speed = Modules.get().get(Speed.class);
        boolean strafeSpeed = speed.isActive() && speed.speedMode.get() == SpeedModes.Strafe;

        if (Modules.get().get(NoSlow.class).cobweb() && !strafeSpeed) ci.cancel();
    }
}
