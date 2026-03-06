/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.addons;

import florencedevelopment.florenceclient.FlorenceClient;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;

import java.util.ArrayList;
import java.util.List;

public class AddonManager {
    public static final List<FlorenceAddon> ADDONS = new ArrayList<>();

    public static void init() {
        // Florence pseudo addon
        {
            FlorenceClient.ADDON = new FlorenceAddon() {
                @Override
                public void onInitialize() {}

                @Override
                public String getPackage() {
                    return "florencedevelopment.florenceclient";
                }

                @Override
                public String getWebsite() {
                    return null;
                }

                @Override
                public GithubRepo getRepo() {
                    return null;
                }

                @Override
                public String getCommit() {
                    String commit = FlorenceClient.MOD_META.getCustomValue(FlorenceClient.MOD_ID + ":commit").getAsString();
                    return commit.isEmpty() ? null : commit;
                }
            };

            ModMetadata metadata = FabricLoader.getInstance().getModContainer(FlorenceClient.MOD_ID).get().getMetadata();

            FlorenceClient.ADDON.name = metadata.getName();
            FlorenceClient.ADDON.authors = new String[metadata.getAuthors().size()];
            if (metadata.containsCustomValue(FlorenceClient.MOD_ID + ":color")) {
                FlorenceClient.ADDON.color.parse(metadata.getCustomValue(FlorenceClient.MOD_ID + ":color").getAsString());
            }

            int i = 0;
            for (Person author : metadata.getAuthors()) {
                FlorenceClient.ADDON.authors[i++] = author.getName();
            }

            ADDONS.add(FlorenceClient.ADDON);
        }

        // Addons
        for (EntrypointContainer<FlorenceAddon> entrypoint : FabricLoader.getInstance().getEntrypointContainers("florence", FlorenceAddon.class)) {
            ModMetadata metadata = entrypoint.getProvider().getMetadata();
            FlorenceAddon addon;
            try {
                addon = entrypoint.getEntrypoint();
            } catch (Throwable throwable) {
                throw new RuntimeException("Exception during addon init \"%s\".".formatted(metadata.getName()), throwable);
            }

            addon.name = metadata.getName();

            if (metadata.getAuthors().isEmpty()) throw new RuntimeException("Addon \"%s\" requires at least 1 author to be defined in it's fabric.mod.json. See https://fabricmc.net/wiki/documentation:fabric_mod_json_spec".formatted(addon.name));
            addon.authors = new String[metadata.getAuthors().size()];

            if (metadata.containsCustomValue(FlorenceClient.MOD_ID + ":color")) {
                addon.color.parse(metadata.getCustomValue(FlorenceClient.MOD_ID + ":color").getAsString());
            }

            int i = 0;
            for (Person author : metadata.getAuthors()) {
                addon.authors[i++] = author.getName();
            }

            ADDONS.add(addon);
        }
    }
}
