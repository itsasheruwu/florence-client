/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.design;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CategoryColorsTest {
    private static final String[] BUILT_IN = {"Combat", "Player", "Movement", "Render", "World", "Misc"};

    @Test
    void colorsAreOpaque() {
        for (String name : BUILT_IN) assertEquals(255, Colors.alpha(CategoryColors.of(name)), name);
    }

    @Test
    void theBuiltInCategoriesAllLookDifferent() {
        Set<Integer> colors = new HashSet<>();

        for (String name : BUILT_IN) colors.add(CategoryColors.of(name));

        assertEquals(BUILT_IN.length, colors.size());
    }

    @Test
    void theSameNameAlwaysGivesTheSameColor() {
        assertEquals(CategoryColors.of("Combat"), CategoryColors.of("Combat"));
        assertEquals(CategoryColors.of("My Addon"), CategoryColors.of("My Addon"));
    }

    @Test
    void caseDoesNotMatterForTheBuiltInCategories() {
        assertEquals(CategoryColors.of("Combat"), CategoryColors.of("COMBAT"));
        assertEquals(CategoryColors.of("misc"), CategoryColors.of("Misc"));
    }

    @Test
    void unknownAndMissingNamesStillGetAColor() {
        assertEquals(255, Colors.alpha(CategoryColors.of("Something New")));
        assertEquals(255, Colors.alpha(CategoryColors.of(null)));
        assertEquals(255, Colors.alpha(CategoryColors.of("")));
    }

    @Test
    void colorsAreVividEnoughToBeSeen() {
        for (String name : BUILT_IN) {
            double[] hsv = Colors.toHsv(CategoryColors.of(name));

            assertTrue(hsv[1] > 0.4, name + " saturation");
            assertTrue(hsv[2] > 0.8, name + " brightness");
        }
    }
}
