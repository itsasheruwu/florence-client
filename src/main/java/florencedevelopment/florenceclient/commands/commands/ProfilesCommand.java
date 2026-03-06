/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import florencedevelopment.florenceclient.commands.Command;
import florencedevelopment.florenceclient.commands.arguments.ProfileArgumentType;
import florencedevelopment.florenceclient.systems.profiles.Profile;
import florencedevelopment.florenceclient.systems.profiles.Profiles;
import net.minecraft.command.CommandSource;

public class ProfilesCommand extends Command {

    public ProfilesCommand() {
        super("profiles", "Loads and saves profiles.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("load").then(argument("profile", ProfileArgumentType.create()).executes(context -> {
            Profile profile = ProfileArgumentType.get(context);

            if (profile != null) {
                profile.load();
                info("Loaded profile (highlight)%s(default).", profile.name.get());
            }

            return SINGLE_SUCCESS;
        })));

        builder.then(literal("save").then(argument("profile", ProfileArgumentType.create()).executes(context -> {
            Profile profile = ProfileArgumentType.get(context);

            if (profile != null) {
                profile.save();
                info("Saved profile (highlight)%s(default).", profile.name.get());
            }

            return SINGLE_SUCCESS;
        })));

        builder.then(literal("delete").then(argument("profile", ProfileArgumentType.create()).executes(context -> {
            Profile profile = ProfileArgumentType.get(context);

            if (profile != null) {
                Profiles.get().remove(profile);
                info("Deleted profile (highlight)%s(default).", profile.name.get());
            }

            return SINGLE_SUCCESS;
        })));
    }
}
