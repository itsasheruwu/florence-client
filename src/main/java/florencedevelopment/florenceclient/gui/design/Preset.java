/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.design;

import static florencedevelopment.florenceclient.gui.design.Colors.argb;
import static florencedevelopment.florenceclient.gui.design.Colors.rgb;

/**
 * The base look of the GUI: the surfaces, text and lines. The accent color is chosen separately.
 */
public enum Preset {
    DARK("Florence Dark") {
        @Override
        Surfaces surfaces() {
            return new Surfaces(
                rgb(13, 14, 21), rgb(22, 24, 35),
                argb(255, 255, 255, 8), argb(255, 255, 255, 20), argb(255, 255, 255, 26),
                argb(255, 255, 255, 15), argb(255, 255, 255, 36), argb(255, 255, 255, 18),
                rgb(240, 242, 250), rgb(152, 158, 178), rgb(98, 104, 124),
                argb(255, 255, 255, 36), rgb(255, 255, 255),
                argb(4, 5, 10, 120), argb(0, 0, 0, 150),
                false
            );
        }
    },

    MIDNIGHT("Midnight") {
        @Override
        Surfaces surfaces() {
            return new Surfaces(
                rgb(6, 10, 22), rgb(12, 19, 38),
                argb(150, 190, 255, 9), argb(150, 190, 255, 22), argb(150, 190, 255, 28),
                argb(150, 190, 255, 16), argb(150, 190, 255, 40), argb(150, 190, 255, 20),
                rgb(232, 240, 255), rgb(140, 156, 190), rgb(86, 100, 130),
                argb(150, 190, 255, 40), rgb(255, 255, 255),
                argb(2, 6, 18, 130), argb(0, 0, 8, 160),
                false
            );
        }
    },

    LIGHT("Light") {
        @Override
        Surfaces surfaces() {
            return new Surfaces(
                rgb(246, 247, 252), rgb(255, 255, 255),
                argb(20, 24, 50, 9), argb(20, 24, 50, 20), argb(20, 24, 50, 28),
                argb(20, 24, 50, 14), argb(20, 24, 50, 34), argb(20, 24, 50, 18),
                rgb(20, 22, 36), rgb(84, 90, 112), rgb(150, 156, 176),
                argb(20, 24, 50, 40), rgb(255, 255, 255),
                argb(14, 16, 30, 80), argb(30, 36, 70, 70),
                true
            );
        }
    },

    HIGH_CONTRAST("High Contrast") {
        @Override
        Surfaces surfaces() {
            return new Surfaces(
                rgb(0, 0, 0), rgb(10, 10, 10),
                argb(255, 255, 255, 18), argb(255, 255, 255, 40), argb(255, 255, 255, 52),
                argb(255, 255, 255, 40), argb(255, 255, 255, 140), argb(255, 255, 255, 70),
                rgb(255, 255, 255), rgb(214, 214, 214), rgb(150, 150, 150),
                argb(255, 255, 255, 90), rgb(255, 255, 255),
                argb(0, 0, 0, 170), argb(0, 0, 0, 200),
                false
            );
        }
    },

    ENDERSTORM("Enderstorm") {
        @Override
        Surfaces surfaces() {
            return new Surfaces(
                rgb(22, 12, 36), rgb(34, 18, 54),
                argb(216, 180, 254, 10), argb(216, 180, 254, 24), argb(216, 180, 254, 30),
                argb(216, 180, 254, 18), argb(216, 180, 254, 44), argb(216, 180, 254, 22),
                rgb(246, 238, 255), rgb(176, 156, 206), rgb(116, 98, 146),
                argb(216, 180, 254, 42), rgb(255, 255, 255),
                argb(10, 4, 20, 130), argb(6, 0, 14, 160),
                false
            );
        }
    };

    private final String title;

    Preset(String title) {
        this.title = title;
    }

    abstract Surfaces surfaces();

    @Override
    public String toString() {
        return title;
    }

    /**
     * Colors of a preset. The panel and header are opaque here, the opacity the user chose is applied on top.
     */
    record Surfaces(
        int panel, int header,
        int card, int cardHover, int cardPressed,
        int field, int fieldHover, int fieldFocus,
        int text, int textSecondary, int textDisabled,
        int outline, int thumb,
        int scrim, int shadow,
        boolean light
    ) {}
}
