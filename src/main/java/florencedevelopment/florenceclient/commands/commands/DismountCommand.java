/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import florencedevelopment.florenceclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.util.PlayerInput;

public class DismountCommand extends Command {
    public DismountCommand() {
        super("dismount", "Dismounts you from entity you are riding.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            PlayerInput sneak = new PlayerInput(false, false, false, false, false, true, false);
            mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(sneak));
            return SINGLE_SUCCESS;
        });
    }
}
