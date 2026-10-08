/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.modules;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LegitModulesTest {
    @Test
    void visualAndQualityOfLifeModulesAreLegit() {
        assertTrue(LegitModules.isDefault("zoom"));
        assertTrue(LegitModules.isDefault("better-tab"));
    }

    @Test
    void combatAndExploitModulesAreNot() {
        assertFalse(LegitModules.isDefault("anchor-aura"));
        assertFalse(LegitModules.isDefault("offhand-crash"));
        assertFalse(LegitModules.isDefault("nuker"));
    }

    @Test
    void unknownNamesAreNot() {
        assertFalse(LegitModules.isDefault(""));
        assertFalse(LegitModules.isDefault("Zoom"));
    }
}
