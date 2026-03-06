/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import florencedevelopment.florenceclient.commands.Command;
import florencedevelopment.florenceclient.renderer.Fonts;
import florencedevelopment.florenceclient.systems.Systems;
import florencedevelopment.florenceclient.systems.friends.Friend;
import florencedevelopment.florenceclient.systems.friends.Friends;
import florencedevelopment.florenceclient.utils.network.Capes;
import florencedevelopment.florenceclient.utils.network.FlorenceExecutor;
import net.minecraft.command.CommandSource;

public class ReloadCommand extends Command {
    public ReloadCommand() {
        super("reload", "Reloads many systems.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            warning("Reloading systems, this may take a while.");

            Systems.load();
            Capes.init();
            Fonts.refresh();
            FlorenceExecutor.execute(() -> Friends.get().forEach(Friend::updateInfo));

            return SINGLE_SUCCESS;
        });
    }
}
