/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.modules.world;

import florencedevelopment.florenceclient.events.packets.PacketEvent;
import florencedevelopment.florenceclient.mixin.BlockHitResultAccessor;
import florencedevelopment.florenceclient.systems.modules.Categories;
import florencedevelopment.florenceclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.math.Direction;

public class BuildHeight extends Module {
    public BuildHeight() {
        super(Categories.World, "build-height", "Allows you to interact with objects at the build limit.");
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (!(event.packet instanceof PlayerInteractBlockC2SPacket p)) return;
        if (mc.world == null) return;
        if (p.getBlockHitResult().getPos().y >= mc.world.getHeight() && p.getBlockHitResult().getSide() == Direction.UP) {
            ((BlockHitResultAccessor) p.getBlockHitResult()).florence$setSide(Direction.DOWN);
        }
    }
}
