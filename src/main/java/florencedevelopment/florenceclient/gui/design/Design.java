/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.design;

import static florencedevelopment.florenceclient.gui.design.Colors.*;

/**
 * The colors the GUI is drawn with, worked out from a {@link Preset}, the accent color and the opacity of the panels.
 * Every color is packed as 0xAARRGGBB, see {@link Colors}. Instances never change, a new one is built when a setting
 * does.
 */
public final class Design {
    public final Preset preset;
    public final boolean light;

    // Surfaces
    /** Body of windows. Translucent so the blurred world shows through when glass is on. */
    public final int panel;
    /** Body of windows when there is no blurred world behind them to show through. */
    public final int panelSolid;
    public final int header;
    /** Resting, hovered, pressed and active rows and buttons. */
    public final int card, cardHover, cardPressed, cardActive;
    /** Inputs. */
    public final int field, fieldHover, fieldFocus;
    public final int outline, outlineHover, divider;

    // Text
    public final int text, textSecondary, textDisabled, textOnAccent;

    // Accent
    public final int accent, accentHover, accentPressed, accentSoft, accent2;

    // Status
    public final int success, warning, danger, favorite;

    // Other
    public final int scrim, shadow, thumb, trackOff;

    private Design(Preset preset, int accent, int accent2, double panelOpacity) {
        Preset.Surfaces s = preset.surfaces();

        this.preset = preset;
        this.light = s.light();

        panelOpacity = Math.max(0.2, Math.min(1, panelOpacity));

        panel = withAlpha(s.panel(), (int) Math.round(255 * panelOpacity));
        panelSolid = withAlpha(s.panel(), 246);
        header = withAlpha(s.header(), (int) Math.round(255 * Math.min(1, panelOpacity + 0.1)));

        card = s.card();
        cardHover = s.cardHover();
        cardPressed = s.cardPressed();

        field = s.field();
        fieldHover = s.fieldHover();
        fieldFocus = s.fieldFocus();

        outline = s.outline();
        outlineHover = withAlpha(s.outline(), Math.min(255, alpha(s.outline()) * 2));
        divider = withAlpha(s.outline(), Math.round(alpha(s.outline()) * 0.6f));

        text = s.text();
        textSecondary = s.textSecondary();
        textDisabled = s.textDisabled();

        this.accent = withAlpha(accent, 255);
        accentHover = light ? darken(this.accent, 0.1) : lighten(this.accent, 0.14);
        accentPressed = light ? darken(this.accent, 0.22) : darken(this.accent, 0.16);
        accentSoft = withAlpha(accent, 44);
        this.accent2 = withAlpha(accent2, 255);

        // Whichever of white and near black is easier to read on the accent
        int dark = rgb(12, 13, 20);
        int white = rgb(255, 255, 255);
        textOnAccent = contrast(white, this.accent) >= contrast(dark, this.accent) ? white : dark;

        cardActive = withAlpha(accent, light ? 38 : 50);

        success = light ? rgb(22, 163, 74) : rgb(74, 222, 128);
        warning = light ? rgb(217, 119, 6) : rgb(251, 191, 36);
        danger = light ? rgb(220, 38, 38) : rgb(248, 113, 113);
        favorite = light ? rgb(202, 138, 4) : rgb(250, 204, 21);

        scrim = s.scrim();
        shadow = s.shadow();
        thumb = s.thumb();
        trackOff = light ? argb(20, 24, 50, 46) : argb(255, 255, 255, 40);
    }

    /**
     * @param panelOpacity how opaque window bodies are, from 0.2 to 1
     */
    public static Design build(Preset preset, int accent, int accent2, double panelOpacity) {
        return new Design(preset, accent, accent2, panelOpacity);
    }
}
