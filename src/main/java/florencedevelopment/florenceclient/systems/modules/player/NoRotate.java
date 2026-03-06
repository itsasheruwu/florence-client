/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.modules.player;

import florencedevelopment.florenceclient.events.packets.PacketEvent;
import florencedevelopment.florenceclient.systems.modules.Categories;
import florencedevelopment.florenceclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EntityPosition;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class NoRotate extends Module {
    public NoRotate() {
        super(Categories.Player, "no-rotate", "Attempts to block rotations sent from server to client.");
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket packet) {
            EntityPosition oldPosition = packet.change();
            EntityPosition newPosition = new EntityPosition(
                oldPosition.position(),
                oldPosition.deltaMovement(),
                mc.player.getYaw(),
                mc.player.getPitch()
            );
            event.packet = PlayerPositionLookS2CPacket.of(
                packet.teleportId(),
                newPosition,
                packet.relatives()
            );
        }
    }
}
