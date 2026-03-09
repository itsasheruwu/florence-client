/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import florencedevelopment.florenceclient.commands.Command;
import florencedevelopment.florenceclient.commands.arguments.ModuleArgumentType;
import florencedevelopment.florenceclient.systems.modules.Module;
import net.minecraft.command.CommandSource;

public class UnbindCommand extends Command {
    public UnbindCommand() {
        super("unbind", "Removes the keybind from a specified module.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("module", ModuleArgumentType.create()).executes(context -> {
            Module module = context.getArgument("module", Module.class);

            if (!module.keybind.isSet()) {
                module.info("Module is already unbound.");
                return SINGLE_SUCCESS;
            }

            module.keybind.reset();
            module.info("Removed bind.");
            return SINGLE_SUCCESS;
        }));
    }
}
