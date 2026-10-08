/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThemeFilesTest {
    @TempDir
    File folder;

    @Test
    void renamedThemeMapsToCurrentName() {
        assertEquals("Florence", ThemeFiles.currentName("Meteor"));
    }

    @Test
    void otherNamesAreUnchanged() {
        assertEquals("Florence", ThemeFiles.currentName("Florence"));
        assertEquals("Custom", ThemeFiles.currentName("Custom"));
        assertEquals("", ThemeFiles.currentName(""));
    }

    @Test
    void resolvePrefersTheCurrentFile() throws IOException {
        File current = new File(folder, "Florence.nbt");
        new File(folder, "Meteor.nbt").createNewFile();
        current.createNewFile();

        assertEquals(current, ThemeFiles.resolve(folder, "Florence"));
    }

    @Test
    void resolveFallsBackToTheFormerNameUntilTheThemeIsSaved() throws IOException {
        File legacy = new File(folder, "Meteor.nbt");
        legacy.createNewFile();

        assertEquals(legacy, ThemeFiles.resolve(folder, "Florence"));
    }

    @Test
    void resolveReturnsTheCurrentPathWhenNothingExists() {
        assertEquals(new File(folder, "Florence.nbt"), ThemeFiles.resolve(folder, "Florence"));
    }

    @Test
    void resolveDoesNotUseUnrelatedFiles() throws IOException {
        new File(folder, "Meteor.nbt").createNewFile();

        assertEquals(new File(folder, "Custom.nbt"), ThemeFiles.resolve(folder, "Custom"));
    }
}
