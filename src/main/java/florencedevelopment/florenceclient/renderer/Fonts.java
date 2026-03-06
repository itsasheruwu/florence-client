/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.renderer;

import florencedevelopment.florenceclient.FlorenceClient;
import florencedevelopment.florenceclient.events.florence.CustomFontChangedEvent;
import florencedevelopment.florenceclient.gui.WidgetScreen;
import florencedevelopment.florenceclient.renderer.text.CustomTextRenderer;
import florencedevelopment.florenceclient.renderer.text.FontFace;
import florencedevelopment.florenceclient.renderer.text.FontFamily;
import florencedevelopment.florenceclient.renderer.text.FontInfo;
import florencedevelopment.florenceclient.systems.config.Config;
import florencedevelopment.florenceclient.utils.PreInit;
import florencedevelopment.florenceclient.utils.render.FontUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static florencedevelopment.florenceclient.FlorenceClient.mc;

public class Fonts {
    public static final String[] BUILTIN_FONTS = { };

    public static String DEFAULT_FONT_FAMILY = "Tahoma";
    public static FontFace DEFAULT_FONT;

    public static final List<FontFamily> FONT_FAMILIES = new ArrayList<>();
    public static CustomTextRenderer RENDERER;

    private Fonts() {
    }

    @PreInit
    public static void refresh() {
        FONT_FAMILIES.clear();

        for (String builtinFont : BUILTIN_FONTS) {
            FontUtils.loadBuiltin(FONT_FAMILIES, builtinFont);
        }

        for (String fontPath : FontUtils.getSearchPaths()) {
            FontUtils.loadSystem(FONT_FAMILIES, new File(fontPath));
        }

        FONT_FAMILIES.sort(Comparator.comparing(FontFamily::getName));

        // Filter to only include Tahoma
        FONT_FAMILIES.removeIf(family -> !family.getName().equalsIgnoreCase("Tahoma"));

        FlorenceClient.LOG.info("Found {} font families (filtered to Tahoma only).", FONT_FAMILIES.size());

        // Set Tahoma as default font (system font)
        FontFamily tahomaFamily = getFamily(DEFAULT_FONT_FAMILY);
        if (tahomaFamily != null) {
            // Try Regular first, then Bold, then any other type
            DEFAULT_FONT = tahomaFamily.get(FontInfo.Type.Regular);
            if (DEFAULT_FONT == null) {
                DEFAULT_FONT = tahomaFamily.get(FontInfo.Type.Bold);
            }
            if (DEFAULT_FONT == null) {
                DEFAULT_FONT = tahomaFamily.get(FontInfo.Type.Italic);
            }
            if (DEFAULT_FONT == null) {
                DEFAULT_FONT = tahomaFamily.get(FontInfo.Type.BoldItalic);
            }
        }
        
        // Fallback to first available font if Tahoma is not found
        if (DEFAULT_FONT == null && !FONT_FAMILIES.isEmpty()) {
            FontFamily firstFamily = FONT_FAMILIES.get(0);
            DEFAULT_FONT = firstFamily.get(FontInfo.Type.Regular);
            if (DEFAULT_FONT == null) {
                DEFAULT_FONT = firstFamily.get(FontInfo.Type.Bold);
            }
            if (DEFAULT_FONT == null && !firstFamily.hasType(FontInfo.Type.Regular)) {
                // Try to find any font type
                for (FontInfo.Type type : FontInfo.Type.values()) {
                    DEFAULT_FONT = firstFamily.get(type);
                    if (DEFAULT_FONT != null) break;
                }
            }
            if (DEFAULT_FONT != null) {
                DEFAULT_FONT_FAMILY = firstFamily.getName();
                FlorenceClient.LOG.warn("Tahoma font not found, using {} as default.", DEFAULT_FONT_FAMILY);
            }
        }

        Config config = Config.get();
        load(config != null ? config.font.get() : DEFAULT_FONT);
    }

    public static void load(FontFace fontFace) {
        if (RENDERER != null) {
            if (RENDERER.fontFace.equals(fontFace)) return;
            else RENDERER.destroy();
        }

        try {
            RENDERER = new CustomTextRenderer(fontFace);
            FlorenceClient.EVENT_BUS.post(CustomFontChangedEvent.get());
        }
        catch (Exception e) {
            if (fontFace.equals(DEFAULT_FONT)) {
                throw new RuntimeException("Failed to load default font: " + fontFace, e);
            }

            FlorenceClient.LOG.error("Failed to load font: {}", fontFace, e);
            load(Fonts.DEFAULT_FONT);
        }

        if (mc.currentScreen instanceof WidgetScreen && Config.get().customFont.get()) {
            ((WidgetScreen) mc.currentScreen).invalidate();
        }
    }

    public static FontFamily getFamily(String name) {
        for (FontFamily fontFamily : Fonts.FONT_FAMILIES) {
            if (fontFamily.getName().equalsIgnoreCase(name)) {
                return fontFamily;
            }
        }

        return null;
    }
}
