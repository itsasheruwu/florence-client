/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.utils.entity.fakeplayer;

import com.mojang.authlib.GameProfile;
import florencedevelopment.florenceclient.mixin.AbstractClientPlayerEntityAccessor;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static florencedevelopment.florenceclient.FlorenceClient.mc;

public class FakePlayerEntity extends OtherClientPlayerEntity {
    /** Disables entity push with this fake player */
    public boolean doNotPush;
    /** Stops rendering the fake player when you are inside it */
    public boolean hideWhenInsideCamera;
    /** Prevents you from interacting with the fake player; will also prevent TargetUtils selecting it as a target */
    public boolean noHit;

    public FakePlayerEntity(PlayerEntity player, String name, float health, boolean copyInv) {
        super(mc.world, new GameProfile(UUID.randomUUID(), name));

        copyPositionAndRotation(player);

        lastYaw = getYaw();
        lastPitch = getPitch();
        headYaw = player.headYaw;
        lastHeadYaw = headYaw;
        bodyYaw = player.bodyYaw;
        lastBodyYaw = bodyYaw;

        getAttributes().setFrom(player.getAttributes());
        setPose(player.getPose());

        if (health <= 20) {
            setHealth(health);
        } else {
            setHealth(health);
            setAbsorptionAmount(health - 20);
        }

        if (copyInv) getInventory().clone(player.getInventory());
    }

    public void spawn() {
        unsetRemoved();
        mc.world.addEntity(this);
    }

    public void despawn() {
        mc.world.removeEntity(getId(), RemovalReason.DISCARDED);
        setRemoved(RemovalReason.DISCARDED);
    }

    @Nullable
    @Override
    protected PlayerListEntry getPlayerListEntry() {
        PlayerListEntry entry = super.getPlayerListEntry();

        if (entry == null) {
            ((AbstractClientPlayerEntityAccessor) this).florence$setPlayerListEntry(mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid()));
        }

        return entry;
    }
}
