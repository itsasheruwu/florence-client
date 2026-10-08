/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Keeps saved GUI themes working across theme renames.
 */
final class ThemeFiles {
    // Current theme name -> names it was saved under before
    private static final Map<String, List<String>> FORMER_NAMES = Map.of(
        "Florence", List.of("Meteor")
    );

    private ThemeFiles() {}

    /**
     * Maps the name of a theme that has been renamed to its current name, any other name is returned unchanged.
     */
    static String currentName(String name) {
        for (Map.Entry<String, List<String>> entry : FORMER_NAMES.entrySet()) {
            if (entry.getValue().contains(name)) return entry.getKey();
        }

        return name;
    }

    /**
     * Finds the file the settings of a theme are loaded from. This is the file of the theme itself or, if it hasn't been
     * saved yet, the file it was saved to under a former name.
     */
    static File resolve(File folder, String name) {
        File file = new File(folder, name + ".nbt");
        if (file.exists()) return file;

        for (String formerName : FORMER_NAMES.getOrDefault(name, List.of())) {
            File formerFile = new File(folder, formerName + ".nbt");
            if (formerFile.exists()) return formerFile;
        }

        return file;
    }
}
