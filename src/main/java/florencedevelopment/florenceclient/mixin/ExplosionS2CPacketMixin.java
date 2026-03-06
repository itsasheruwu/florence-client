/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin;

import florencedevelopment.florenceclient.mixininterface.IExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(ExplosionS2CPacket.class)
public abstract class ExplosionS2CPacketMixin implements IExplosionS2CPacket {
    @Shadow
    @Final
    @Mutable
    private Optional<Vec3d> playerKnockback;

    @Override
    public void florence$setVelocityX(float velocity) {
        if (playerKnockback.isPresent()) {
            Vec3d kb = playerKnockback.get();
            playerKnockback = Optional.of(new Vec3d(velocity, kb.y, kb.z));
        } else {
            playerKnockback = Optional.of(new Vec3d(velocity, 0, 0));
        }
    }

    @Override
    public void florence$setVelocityY(float velocity) {
        if (playerKnockback.isPresent()) {
            Vec3d kb = playerKnockback.get();
            playerKnockback = Optional.of(new Vec3d(kb.x, velocity, kb.z));
        } else {
            playerKnockback = Optional.of(new Vec3d(0, velocity, 0));
        }
    }

    @Override
    public void florence$setVelocityZ(float velocity) {
        if (playerKnockback.isPresent()) {
            Vec3d kb = playerKnockback.get();
            playerKnockback = Optional.of(new Vec3d(kb.x, kb.y, velocity));
        } else {
            playerKnockback = Optional.of(new Vec3d(0, 0, velocity));
        }
    }
}
