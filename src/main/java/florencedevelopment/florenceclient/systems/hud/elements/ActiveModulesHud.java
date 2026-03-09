/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.hud.elements;

import florencedevelopment.florenceclient.settings.*;
import florencedevelopment.florenceclient.systems.hud.*;
import florencedevelopment.florenceclient.systems.modules.Module;
import florencedevelopment.florenceclient.systems.modules.Modules;
import florencedevelopment.florenceclient.utils.render.color.Color;
import florencedevelopment.florenceclient.utils.render.color.SettingColor;

import java.util.ArrayList;
import java.util.List;

public class ActiveModulesHud extends HudElement {
    public static final HudElementInfo<ActiveModulesHud> INFO = new HudElementInfo<>(Hud.GROUP, "active-modules", "Displays your active modules.", ActiveModulesHud::new);

    private static final Color WHITE = new Color();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColor = settings.createGroup("Color");
    private final SettingGroup sgScale = settings.createGroup("Scale");
    private final SettingGroup sgBackground = settings.createGroup("Background");

    private final Setting<Sort> sort = sgGeneral.add(new EnumSetting.Builder<Sort>()
        .name("sort")
        .description("How to sort active modules.")
        .defaultValue(Sort.Biggest)
        .build()
    );

    private final Setting<List<Module>> hiddenModules = sgGeneral.add(new ModuleListSetting.Builder()
        .name("hidden-modules")
        .description("Which modules not to show in the list.")
        .build()
    );

    private final Setting<Boolean> activeInfo = sgGeneral.add(new BoolSetting.Builder()
        .name("module-info")
        .description("Shows info from the module next to the name in the active modules list.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showKeybind = sgGeneral.add(new BoolSetting.Builder()
        .name("show-keybind")
        .description("Shows the module's keybind next to its name.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> toggleAnimation = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-animation")
        .description("Animates modules when they are toggled on or off in the list.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> toggleAnimationSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("toggle-animation-speed")
        .description("How quickly the toggle animation plays.")
        .defaultValue(24)
        .min(1)
        .sliderRange(1, 60)
        .visible(toggleAnimation::get)
        .build()
    );

    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Renders shadow behind text.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> outlines = sgGeneral.add(new BoolSetting.Builder()
        .name("outlines")
        .description("Whether or not to render outlines")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> outlineWidth = sgGeneral.add(new IntSetting.Builder()
        .name("outline-width")
        .description("Outline width")
        .defaultValue(2)
        .min(1)
        .sliderMin(1)
        .visible(outlines::get)
        .build()
    );

    private final Setting<Alignment> alignment = sgGeneral.add(new EnumSetting.Builder<Alignment>()
        .name("alignment")
        .description("Horizontal alignment.")
        .defaultValue(Alignment.Auto)
        .build()
    );

    // Color

    private final Setting<ColorMode> colorMode = sgColor.add(new EnumSetting.Builder<ColorMode>()
        .name("color-mode")
        .description("What color to use for active modules.")
        .defaultValue(ColorMode.Rainbow)
        .build()
    );

    private final Setting<SettingColor> flatColor = sgColor.add(new ColorSetting.Builder()
        .name("flat-color")
        .description("Color for flat color mode.")
        .defaultValue(new SettingColor(225, 25, 25))
        .visible(() -> colorMode.get() == ColorMode.Flat)
        .build()
    );

    private final Setting<Double> rainbowSpeed = sgColor.add(new DoubleSetting.Builder()
        .name("rainbow-speed")
        .description("Rainbow speed of rainbow color mode.")
        .defaultValue(0.05)
        .sliderMin(0.01)
        .sliderMax(0.2)
        .decimalPlaces(4)
        .visible(() -> colorMode.get() == ColorMode.Rainbow)
        .build()
    );

    private final Setting<Double> rainbowSpread = sgColor.add(new DoubleSetting.Builder()
        .name("rainbow-spread")
        .description("Rainbow spread of rainbow color mode.")
        .defaultValue(0.01)
        .sliderMin(0.001)
        .sliderMax(0.05)
        .decimalPlaces(4)
        .visible(() -> colorMode.get() == ColorMode.Rainbow)
        .build()
    );

    private final Setting<Double> rainbowSaturation = sgColor.add(new DoubleSetting.Builder()
        .name("rainbow-saturation")
        .defaultValue(1.0d)
        .sliderRange(0.0d, 1.0d)
        .visible(() -> colorMode.get() == ColorMode.Rainbow)
        .build()
    );

    private final Setting<Double> rainbowBrightness = sgColor.add(new DoubleSetting.Builder()
        .name("rainbow-brightness")
        .defaultValue(1.0d)
        .sliderRange(0.0d, 1.0d)
        .visible(() -> colorMode.get() == ColorMode.Rainbow)
        .build()
    );

    private final Setting<SettingColor> moduleInfoColor = sgColor.add(new ColorSetting.Builder()
        .name("module-info-color")
        .description("Color of module info text.")
        .defaultValue(new SettingColor(175, 175, 175))
        .visible(activeInfo::get)
        .build()
    );

    // Scale

    private final Setting<Boolean> customScale = sgScale.add(new BoolSetting.Builder()
        .name("custom-scale")
        .description("Applies a custom scale to this hud element.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> scale = sgScale.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Custom scale.")
        .visible(customScale::get)
        .defaultValue(1)
        .min(0.5)
        .sliderRange(0.5, 3)
        .build()
    );

    // Background

    private final Setting<Boolean> background = sgBackground.add(new BoolSetting.Builder()
        .name("background")
        .description("Displays background.")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> backgroundColor = sgBackground.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Color used for the background.")
        .visible(background::get)
        .defaultValue(new SettingColor(25, 25, 25, 50))
        .build()
    );

    private final List<ModuleEntry> moduleEntries = new ArrayList<>();
    private final List<ModuleEntry> visibleEntries = new ArrayList<>();

    private final Color rainbow = new Color(255, 255, 255);
    private double rainbowHue1;
    private double rainbowHue2;

    private double lastX;
    private double emptySpace;
    private double prevTextLength;
    private Color prevColor = new Color();

    public ActiveModulesHud() {
        super(INFO);
    }

    @Override
    public void tick(HudRenderer renderer) {
        List<Module> activeModules = new ArrayList<>();

        for (Module module : Modules.get().getActive()) {
            if (!hiddenModules.get().contains(module)) activeModules.add(module);
        }

        syncEntries(activeModules);
        updateAnimationProgress(renderer);
        rebuildVisibleEntries();

        if (visibleEntries.isEmpty()) {
            if (isInEditor()) {
                setSize(renderer.textWidth("Active Modules", shadow.get(), getScale()), renderer.textHeight(shadow.get(), getScale()));
            }
            return;
        }

        visibleEntries.sort((e1, e2) -> switch (sort.get()) {
            case Alphabetical -> e1.module.title.compareTo(e2.module.title);
            case Biggest -> Double.compare(getModuleWidth(renderer, e2.module), getModuleWidth(renderer, e1.module));
            case Smallest -> Double.compare(getModuleWidth(renderer, e1.module), getModuleWidth(renderer, e2.module));
        });

        double width = 0;
        double height = 0;
        double lineHeight = renderer.textHeight(shadow.get(), getScale());

        for (ModuleEntry entry : visibleEntries) {
            width = Math.max(width, getModuleWidth(renderer, entry.module));
            height += lineHeight * getRenderProgress(entry);
        }

        setSize(width, height);
    }

    @Override
    public void render(HudRenderer renderer) {
        double x = this.x;
        double y = this.y;

        if (visibleEntries.isEmpty()) {
            if (isInEditor()) {
                renderer.text("Active Modules", x, y, WHITE, shadow.get(), getScale());
            }
            return;
        }

        rainbowHue1 += rainbowSpeed.get() * renderer.delta;
        if (rainbowHue1 > 1) rainbowHue1 -= 1;
        else if (rainbowHue1 < -1) rainbowHue1 += 1;

        rainbowHue2 = rainbowHue1;

        lastX = x;
        emptySpace = renderer.textWidth(" ", shadow.get(), getScale());

        for (int i = 0; i < visibleEntries.size(); i++) {
            ModuleEntry entry = visibleEntries.get(i);
            double offset = alignX(getModuleWidth(renderer, entry.module), alignment.get());
            double renderX = renderModule(renderer, entry, i, x + offset, y);

            lastX = renderX;
            y += renderer.textHeight(shadow.get(), getScale()) * getRenderProgress(entry);
        }
    }

    private double renderModule(HudRenderer renderer, ModuleEntry entry, int index, double x, double y) {
        Module module = entry.module;
        double progress = getRenderProgress(entry);
        Color color = flatColor.get();

        switch (colorMode.get()) {
            case Random -> color = module.color;
            case Rainbow -> {
                rainbowHue2 += rainbowSpread.get();
                int c = java.awt.Color.HSBtoRGB((float) rainbowHue2, rainbowSaturation.get().floatValue(), rainbowBrightness.get().floatValue());
                rainbow.r = Color.toRGBAR(c);
                rainbow.g = Color.toRGBAG(c);
                rainbow.b = Color.toRGBAB(c);
                color = rainbow;
            }
        }

        double slideOffset = toggleAnimation.get() ? renderer.textHeight(shadow.get(), getScale()) * (1 - progress) : 0;
        double renderX = x + slideOffset;

        renderer.text(module.title, renderX, y, withAlpha(color, progress), shadow.get(), getScale());

        double textHeight = renderer.textHeight(shadow.get(), getScale());
        double textLength = renderer.textWidth(module.title, shadow.get(), getScale());

        if (showKeybind.get() && module.keybind.isSet()) {
            String keybindStr = " [" + module.keybind + "]";
            renderer.text(keybindStr, renderX + textLength, y, withAlpha(moduleInfoColor.get(), progress), shadow.get(), getScale());
            textLength += renderer.textWidth(keybindStr, shadow.get(), getScale());
        }

        if (activeInfo.get()) {
            String info = module.getInfoString();
            if (info != null) {
                renderer.text(info, renderX + textLength + emptySpace, y, withAlpha(moduleInfoColor.get(), progress), shadow.get(), getScale());
                textLength += emptySpace + renderer.textWidth(info, shadow.get(), getScale());
            }
        }

        double lineStartY = y;
        double lineHeight = textHeight;

        if (outlines.get()) {
            if (index == 0) { // Render top quad for first item in list
                lineStartY -= 2;
                lineHeight += 2;

                renderer.quad(renderX - 2 - outlineWidth.get(), lineStartY - outlineWidth.get(),
                    textLength + 4 + 2 * outlineWidth.get(),
                    outlineWidth.get(), withAlpha(prevColor, progress), withAlpha(prevColor, progress), withAlpha(color, progress), withAlpha(color, progress));
            } else { // In-between quads are rendered above the current line so don't need for the top
                renderer.quad(Math.min(lastX, renderX) - 2 - outlineWidth.get(), Math.max(lastX, renderX) == renderX ? y : y - outlineWidth.get(),
                    (Math.max(lastX, renderX) - 2) - (Math.min(lastX, renderX) - 2 - outlineWidth.get()), outlineWidth.get(),
                    withAlpha(prevColor, progress), withAlpha(prevColor, progress), withAlpha(color, progress), withAlpha(color, progress)); // Left in-between quad

                renderer.quad(Math.min(lastX + prevTextLength, renderX + textLength) + 2, Math.min(lastX + prevTextLength, renderX + textLength) == renderX + textLength ? y : y - outlineWidth.get(),
                    (Math.max(lastX + prevTextLength, renderX + textLength) + 2 + outlineWidth.get()) - (Math.min(lastX + prevTextLength, renderX + textLength) + 2), outlineWidth.get(),
                    withAlpha(prevColor, progress), withAlpha(prevColor, progress), withAlpha(color, progress), withAlpha(color, progress)); // Right in-between quad
            }

            if (index == visibleEntries.size() - 1) { // Render bottom quad for last item in list
                lineHeight += 2;

                renderer.quad(renderX - 2 - outlineWidth.get(), lineStartY + lineHeight,
                    textLength + 4 + 2 * outlineWidth.get(), outlineWidth.get(),
                    withAlpha(prevColor, progress), withAlpha(prevColor, progress), withAlpha(color, progress), withAlpha(color, progress));
            }

            // Left side quad
            renderer.quad(renderX - 2 - outlineWidth.get(), lineStartY, outlineWidth.get(), lineHeight,
                withAlpha(prevColor, progress), withAlpha(prevColor, progress), withAlpha(color, progress), withAlpha(color, progress));

            // Right side quad
            renderer.quad(renderX + textLength + 2, lineStartY, outlineWidth.get(), lineHeight,
                withAlpha(prevColor, progress), withAlpha(prevColor, progress), withAlpha(color, progress), withAlpha(color, progress));
        }

        if (background.get()) {
            renderer.quad(renderX - 2, lineStartY, textLength + 4, lineHeight, withAlpha(backgroundColor.get(), progress));
        }

        prevTextLength = textLength;
        prevColor = color;
        return renderX;
    }

    private void syncEntries(List<Module> activeModules) {
        for (Module module : activeModules) {
            ModuleEntry entry = getEntry(module);
            if (entry == null) moduleEntries.add(new ModuleEntry(module));
            else entry.visible = true;
        }

        for (ModuleEntry entry : moduleEntries) {
            if (!activeModules.contains(entry.module)) entry.visible = false;
        }
    }

    private void updateAnimationProgress(HudRenderer renderer) {
        if (!toggleAnimation.get()) {
            moduleEntries.removeIf(entry -> !entry.visible);
            for (ModuleEntry entry : moduleEntries) entry.progress = 1;
            return;
        }

        double animationDelta = Math.min(1, renderer.delta * toggleAnimationSpeed.get());

        moduleEntries.removeIf(entry -> !entry.visible && entry.progress <= 0);
        for (ModuleEntry entry : moduleEntries) {
            entry.progress += animationDelta * (entry.visible ? 1 : -1);

            if (entry.progress < 0) entry.progress = 0;
            else if (entry.progress > 1) entry.progress = 1;
        }
    }

    private void rebuildVisibleEntries() {
        visibleEntries.clear();

        for (ModuleEntry entry : moduleEntries) {
            if (entry.visible || entry.progress > 0) visibleEntries.add(entry);
        }
    }

    private ModuleEntry getEntry(Module module) {
        for (ModuleEntry entry : moduleEntries) {
            if (entry.module.equals(module)) return entry;
        }

        return null;
    }

    private double getRenderProgress(ModuleEntry entry) {
        return toggleAnimation.get() ? entry.progress : 1;
    }

    private Color withAlpha(Color color, double progress) {
        return new Color(color.r, color.g, color.b, (int) Math.round(color.a * progress));
    }

    private double getModuleWidth(HudRenderer renderer, Module module) {
        double width = renderer.textWidth(module.title, shadow.get(), getScale());

        if (showKeybind.get() && module.keybind.isSet()) {
            width += renderer.textWidth(" [" + module.keybind + "]", shadow.get(), getScale());
        }

        if (activeInfo.get()) {
            String info = module.getInfoString();
            if (info != null) width += renderer.textWidth(" ", shadow.get(), getScale()) + renderer.textWidth(info, shadow.get(), getScale());
        }

        return width;
    }

    private double getScale() {
        return customScale.get() ? scale.get() : Hud.get().getTextScale();
    }

    public enum Sort {
        Alphabetical,
        Biggest,
        Smallest
    }

    public enum ColorMode {
        Flat,
        Random,
        Rainbow
    }

    public enum Background {
        None,
        Block,
        Text
    }

    private static class ModuleEntry {
        private final Module module;
        private boolean visible = true;
        private double progress = 0;

        private ModuleEntry(Module module) {
            this.module = module;
        }
    }
}
