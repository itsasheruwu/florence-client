/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.design;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DesignTest {
    private static final int ACCENT = Colors.rgb(108, 92, 231);
    private static final int ACCENT_2 = Colors.rgb(74, 144, 255);

    @Test
    void textIsReadableOnThePanelInEveryPreset() {
        for (Preset preset : Preset.values()) {
            Design design = Design.build(preset, ACCENT, ACCENT_2, 0.85);

            assertTrue(Colors.contrast(design.text, design.panelSolid) >= 7, preset + " text");
            assertTrue(Colors.contrast(design.textSecondary, design.panelSolid) >= 4.5, preset + " secondary text");
            assertTrue(Colors.contrast(design.text, design.header) >= 7, preset + " text on header");
        }
    }

    @Test
    void textOnTheAccentIsReadableForDarkAndLightAccents() {
        int[] accents = {ACCENT, Colors.rgb(250, 204, 21), Colors.rgb(30, 30, 60), Colors.rgb(255, 255, 255), Colors.rgb(0, 200, 120), Colors.rgb(124, 106, 255), Colors.rgb(120, 120, 120)};

        for (int accent : accents) {
            Design design = Design.build(Preset.DARK, accent, accent, 0.85);

            assertTrue(Colors.contrast(design.textOnAccent, design.accent) >= 4.3, "accent " + Integer.toHexString(accent));
        }
    }

    @Test
    void theDefaultAccentTakesWhiteText() {
        Design design = Design.build(Preset.DARK, ACCENT, ACCENT_2, 0.85);

        assertEquals(Colors.rgb(255, 255, 255), design.textOnAccent);
        assertTrue(Colors.contrast(design.textOnAccent, design.accent) >= 4.5);
    }

    @Test
    void panelOpacityOnlyChangesThePanels() {
        Design translucent = Design.build(Preset.DARK, ACCENT, ACCENT_2, 0.4);
        Design opaque = Design.build(Preset.DARK, ACCENT, ACCENT_2, 1);

        assertEquals(102, Colors.alpha(translucent.panel));
        assertEquals(255, Colors.alpha(opaque.panel));
        assertEquals(translucent.text, opaque.text);
        assertEquals(translucent.panelSolid, opaque.panelSolid);
    }

    @Test
    void panelOpacityIsClamped() {
        assertEquals(51, Colors.alpha(Design.build(Preset.DARK, ACCENT, ACCENT_2, -3).panel));
        assertEquals(255, Colors.alpha(Design.build(Preset.DARK, ACCENT, ACCENT_2, 9).panel));
    }

    @Test
    void accentStatesAreOpaqueAndDistinct() {
        Design design = Design.build(Preset.DARK, Colors.argb(124, 106, 255, 10), ACCENT_2, 0.85);

        assertEquals(255, Colors.alpha(design.accent));
        assertEquals(255, Colors.alpha(design.accentHover));
        assertNotEquals(design.accent, design.accentHover);
        assertNotEquals(design.accent, design.accentPressed);
        assertTrue(Colors.alpha(design.accentSoft) < 255);
    }

    @Test
    void lightPresetIsFlaggedAsLight() {
        assertTrue(Design.build(Preset.LIGHT, ACCENT, ACCENT_2, 0.85).light);
        assertFalse(Design.build(Preset.DARK, ACCENT, ACCENT_2, 0.85).light);
    }

    @Test
    void presetsHaveReadableNames() {
        assertEquals("Florence Dark", Preset.DARK.toString());
        assertEquals("High Contrast", Preset.HIGH_CONTRAST.toString());
    }
}
