/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import florencedevelopment.florenceclient.commands.Command;
import florencedevelopment.florenceclient.mixininterface.ISimpleOption;
import net.minecraft.command.CommandSource;

public class FovCommand extends Command {
    public FovCommand() {
        super("fov", "Changes your fov.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("fov", IntegerArgumentType.integer(1, 180)).executes(context -> {
            ((ISimpleOption) (Object) mc.options.getFov()).florence$set(context.getArgument("fov", Integer.class));
            return SINGLE_SUCCESS;
        }));
    }
}
