/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence;

import florencedevelopment.florenceclient.gui.DefaultSettingsWidgetFactory;
import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.WidgetScreen;
import florencedevelopment.florenceclient.gui.renderer.packer.GuiTexture;
import florencedevelopment.florenceclient.gui.themes.florence.widgets.*;
import florencedevelopment.florenceclient.gui.themes.florence.widgets.WFlorenceExpandableModule;
import florencedevelopment.florenceclient.gui.themes.florence.widgets.input.WFlorenceDropdown;
import florencedevelopment.florenceclient.gui.themes.florence.widgets.input.WFlorenceSlider;
import florencedevelopment.florenceclient.gui.themes.florence.widgets.input.WFlorenceTextBox;
import florencedevelopment.florenceclient.gui.themes.florence.widgets.pressable.*;
import florencedevelopment.florenceclient.gui.utils.AlignmentX;
import florencedevelopment.florenceclient.gui.utils.CharFilter;
import florencedevelopment.florenceclient.gui.widgets.*;
import florencedevelopment.florenceclient.gui.widgets.containers.WSection;
import florencedevelopment.florenceclient.gui.widgets.containers.WView;
import florencedevelopment.florenceclient.gui.widgets.containers.WWindow;
import florencedevelopment.florenceclient.gui.widgets.input.WDropdown;
import florencedevelopment.florenceclient.gui.widgets.input.WSlider;
import florencedevelopment.florenceclient.gui.widgets.input.WTextBox;
import florencedevelopment.florenceclient.gui.widgets.pressable.*;
import florencedevelopment.florenceclient.renderer.text.TextRenderer;
import florencedevelopment.florenceclient.settings.*;
import florencedevelopment.florenceclient.systems.accounts.Account;
import florencedevelopment.florenceclient.systems.modules.Module;
import florencedevelopment.florenceclient.utils.render.color.Color;
import florencedevelopment.florenceclient.utils.render.color.SettingColor;
import net.minecraft.client.util.MacWindowUtil;

import static florencedevelopment.florenceclient.FlorenceClient.mc;

public class FlorenceGuiTheme extends GuiTheme {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColors = settings.createGroup("Colors");
    private final SettingGroup sgTextColors = settings.createGroup("Text");
    private final SettingGroup sgBackgroundColors = settings.createGroup("Background");
    private final SettingGroup sgOutline = settings.createGroup("Outline");
    private final SettingGroup sgSeparator = settings.createGroup("Separator");
    private final SettingGroup sgScrollbar = settings.createGroup("Scrollbar");
    private final SettingGroup sgSlider = settings.createGroup("Slider");
    private final SettingGroup sgStarscript = settings.createGroup("Starscript");

    // General

    public final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Scale of the GUI.")
        .defaultValue(1)
        .min(0.75)
        .sliderRange(0.75, 4)
        .onSliderRelease()
        .onChanged(aDouble -> {
            if (mc.currentScreen instanceof WidgetScreen) ((WidgetScreen) mc.currentScreen).invalidate();
        })
        .build()
    );

    public final Setting<AlignmentX> moduleAlignment = sgGeneral.add(new EnumSetting.Builder<AlignmentX>()
        .name("module-alignment")
        .description("How module titles are aligned.")
        .defaultValue(AlignmentX.Center)
        .build()
    );

    public final Setting<Boolean> categoryIcons = sgGeneral.add(new BoolSetting.Builder()
        .name("category-icons")
        .description("Adds item icons to module categories.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> hideHUD = sgGeneral.add(new BoolSetting.Builder()
        .name("hide-HUD")
        .description("Hide HUD when in GUI.")
        .defaultValue(false)
        .onChanged(v -> {
            if (mc.currentScreen instanceof WidgetScreen) mc.options.hudHidden = v;
        })
        .build()
    );

    public final Setting<Boolean> roundedCorners = sgGeneral.add(new BoolSetting.Builder()
        .name("rounded-corners")
        .description("Enable rounded corners for modern look.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Double> cornerRadius = sgGeneral.add(new DoubleSetting.Builder()
        .name("corner-radius")
        .description("Radius of rounded corners.")
        .defaultValue(6)
        .min(0)
        .max(20)
        .sliderRange(0, 20)
        .visible(() -> roundedCorners.get())
        .build()
    );

    public final Setting<Boolean> enableShadows = sgGeneral.add(new BoolSetting.Builder()
        .name("enable-shadows")
        .description("Enable shadows for depth effect.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> smoothAnimations = sgGeneral.add(new BoolSetting.Builder()
        .name("smooth-animations")
        .description("Enable smooth animations and transitions.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> expandableModules = sgGeneral.add(new BoolSetting.Builder()
        .name("expandable-modules")
        .description("Use dropdown/expandable module style with inline settings.")
        .defaultValue(true)
        .onChanged(v -> {
            if (mc.currentScreen instanceof WidgetScreen) ((WidgetScreen) mc.currentScreen).reload();
        })
        .build()
    );

    // Colors

    public final Setting<SettingColor> accentColor = color("accent", "Main color of the GUI.", new SettingColor(100, 150, 255));
    public final Setting<SettingColor> accentSecondaryColor = color("accent-secondary", "Secondary accent color for gradients.", new SettingColor(150, 100, 255));
    public final Setting<SettingColor> checkboxColor = color("checkbox", "Color of checkbox.", new SettingColor(100, 150, 255));
    public final Setting<SettingColor> plusColor = color("plus", "Color of plus button.", new SettingColor(50, 255, 150));
    public final Setting<SettingColor> minusColor = color("minus", "Color of minus button.", new SettingColor(255, 100, 100));
    public final Setting<SettingColor> favoriteColor = color("favorite", "Color of checked favorite button.", new SettingColor(255, 215, 0));

    // Text

    public final Setting<SettingColor> textColor = color(sgTextColors, "text", "Color of text.", new SettingColor(255, 255, 255));
    public final Setting<SettingColor> textSecondaryColor = color(sgTextColors, "text-secondary-text", "Color of secondary text.", new SettingColor(150, 150, 150));
    public final Setting<SettingColor> textHighlightColor = color(sgTextColors, "text-highlight", "Color of text highlighting.", new SettingColor(45, 125, 245, 100));
    public final Setting<SettingColor> titleTextColor = color(sgTextColors, "title-text", "Color of title text.", new SettingColor(255, 255, 255));
    public final Setting<SettingColor> loggedInColor = color(sgTextColors, "logged-in-text", "Color of logged in account name.", new SettingColor(45, 225, 45));
    public final Setting<SettingColor> placeholderColor = color(sgTextColors, "placeholder", "Color of placeholder text.", new SettingColor(255, 255, 255, 20));

    // Background

    public final ThreeStateColorSetting backgroundColor = new ThreeStateColorSetting(
            sgBackgroundColors,
            "background",
            new SettingColor(15, 15, 20, 240),
            new SettingColor(25, 25, 35, 240),
            new SettingColor(35, 35, 50, 240)
    );

    public final Setting<SettingColor> moduleBackground = color(sgBackgroundColors, "module-background", "Color of module background when active.", new SettingColor(40, 50, 70, 200));
    public final Setting<SettingColor> glassEffectColor = color(sgBackgroundColors, "glass-effect", "Color for glassmorphism effect.", new SettingColor(255, 255, 255, 10));

    // Outline

    public final ThreeStateColorSetting outlineColor = new ThreeStateColorSetting(
            sgOutline,
            "outline",
            new SettingColor(50, 70, 100, 150),
            new SettingColor(80, 110, 150, 200),
            new SettingColor(100, 140, 200, 255)
    );

    // Separator

    public final Setting<SettingColor> separatorText = color(sgSeparator, "separator-text", "Color of separator text", new SettingColor(255, 255, 255));
    public final Setting<SettingColor> separatorCenter = color(sgSeparator, "separator-center", "Center color of separators.", new SettingColor(255, 255, 255));
    public final Setting<SettingColor> separatorEdges = color(sgSeparator, "separator-edges", "Color of separator edges.", new SettingColor(225, 225, 225, 150));

    // Scrollbar

    public final ThreeStateColorSetting scrollbarColor = new ThreeStateColorSetting(
            sgScrollbar,
            "Scrollbar",
            new SettingColor(30, 30, 30, 200),
            new SettingColor(40, 40, 40, 200),
            new SettingColor(50, 50, 50, 200)
    );

    // Slider

    public final ThreeStateColorSetting sliderHandle = new ThreeStateColorSetting(
            sgSlider,
            "slider-handle",
            new SettingColor(130, 0, 255),
            new SettingColor(140, 30, 255),
            new SettingColor(150, 60, 255)
    );

    public final Setting<SettingColor> sliderLeft = color(sgSlider, "slider-left", "Color of slider left part.", new SettingColor(100,35,170));
    public final Setting<SettingColor> sliderRight = color(sgSlider, "slider-right", "Color of slider right part.", new SettingColor(50, 50, 50));

    // Starscript

    private final Setting<SettingColor> starscriptText = color(sgStarscript, "starscript-text", "Color of text in Starscript code.", new SettingColor(169, 183, 198));
    private final Setting<SettingColor> starscriptBraces = color(sgStarscript, "starscript-braces", "Color of braces in Starscript code.", new SettingColor(150, 150, 150));
    private final Setting<SettingColor> starscriptParenthesis = color(sgStarscript, "starscript-parenthesis", "Color of parenthesis in Starscript code.", new SettingColor(169, 183, 198));
    private final Setting<SettingColor> starscriptDots = color(sgStarscript, "starscript-dots", "Color of dots in starscript code.", new SettingColor(169, 183, 198));
    private final Setting<SettingColor> starscriptCommas = color(sgStarscript, "starscript-commas", "Color of commas in starscript code.", new SettingColor(169, 183, 198));
    private final Setting<SettingColor> starscriptOperators = color(sgStarscript, "starscript-operators", "Color of operators in Starscript code.", new SettingColor(169, 183, 198));
    private final Setting<SettingColor> starscriptStrings = color(sgStarscript, "starscript-strings", "Color of strings in Starscript code.", new SettingColor(106, 135, 89));
    private final Setting<SettingColor> starscriptNumbers = color(sgStarscript, "starscript-numbers", "Color of numbers in Starscript code.", new SettingColor(104, 141, 187));
    private final Setting<SettingColor> starscriptKeywords = color(sgStarscript, "starscript-keywords", "Color of keywords in Starscript code.", new SettingColor(204, 120, 50));
    private final Setting<SettingColor> starscriptAccessedObjects = color(sgStarscript, "starscript-accessed-objects", "Color of accessed objects (before a dot) in Starscript code.", new SettingColor(152, 118, 170));

    public FlorenceGuiTheme() {
        super("Meteor");

        settingsFactory = new DefaultSettingsWidgetFactory(this);
    }

    private Setting<SettingColor> color(SettingGroup group, String name, String description, SettingColor color) {
        return group.add(new ColorSetting.Builder()
                .name(name + "-color")
                .description(description)
                .defaultValue(color)
                .build());
    }
    private Setting<SettingColor> color(String name, String description, SettingColor color) {
        return color(sgColors, name, description, color);
    }

    // Widgets

    @Override
    public WWindow window(WWidget icon, String title) {
        return w(new WFlorenceWindow(icon, title));
    }

    @Override
    public WLabel label(String text, boolean title, double maxWidth) {
        if (maxWidth == 0 && !text.contains("\n")) return w(new WFlorenceLabel(text, title));
        return w(new WFlorenceMultiLabel(text, title, maxWidth));
    }

    @Override
    public WHorizontalSeparator horizontalSeparator(String text) {
        return w(new WFlorenceHorizontalSeparator(text));
    }

    @Override
    public WVerticalSeparator verticalSeparator() {
        return w(new WFlorenceVerticalSeparator());
    }

    @Override
    protected WButton button(String text, GuiTexture texture) {
        return w(new WFlorenceButton(text, texture));
    }

    @Override
    protected WConfirmedButton confirmedButton(String text, String confirmText, GuiTexture texture) {
        return w(new WFlorenceConfirmedButton(text, confirmText, texture));
    }

    @Override
    public WMinus minus() {
        return w(new WFlorenceMinus());
    }

    @Override
    public WConfirmedMinus confirmedMinus() {
        return w(new WFlorenceConfirmedMinus());
    }

    @Override
    public WPlus plus() {
        return w(new WFlorencePlus());
    }

    @Override
    public WCheckbox checkbox(boolean checked) {
        return w(new WFlorenceCheckbox(checked));
    }

    @Override
    public WSlider slider(double value, double min, double max) {
        return w(new WFlorenceSlider(value, min, max));
    }

    @Override
    public WTextBox textBox(String text, String placeholder, CharFilter filter, Class<? extends WTextBox.Renderer> renderer) {
        return w(new WFlorenceTextBox(text, placeholder, filter, renderer));
    }

    @Override
    public <T> WDropdown<T> dropdown(T[] values, T value) {
        return w(new WFlorenceDropdown<>(values, value));
    }

    @Override
    public WTriangle triangle() {
        return w(new WFlorenceTriangle());
    }

    @Override
    public WTooltip tooltip(String text) {
        return w(new WFlorenceTooltip(text));
    }

    @Override
    public WView view() {
        return w(new WFlorenceView());
    }

    @Override
    public WSection section(String title, boolean expanded, WWidget headerWidget) {
        return w(new WFlorenceSection(title, expanded, headerWidget));
    }

    @Override
    public WAccount account(WidgetScreen screen, Account<?> account) {
        return w(new WFlorenceAccount(screen, account));
    }

    @Override
    public WWidget module(Module module, String title) {
        if (expandableModules.get()) {
            return w(new WFlorenceExpandableModule(module, title));
        }
        return w(new WFlorenceModule(module, title));
    }

    @Override
    public WQuad quad(Color color) {
        return w(new WFlorenceQuad(color));
    }

    @Override
    public WTopBar topBar() {
        return w(new WFlorenceTopBar());
    }

    @Override
    public WFavorite favorite(boolean checked) {
        return w(new WFlorenceFavorite(checked));
    }

    // Colors

    @Override
    public Color textColor() {
        return textColor.get();
    }

    @Override
    public Color textSecondaryColor() {
        return textSecondaryColor.get();
    }

    //     Starscript

    @Override
    public Color starscriptTextColor() {
        return starscriptText.get();
    }

    @Override
    public Color starscriptBraceColor() {
        return starscriptBraces.get();
    }

    @Override
    public Color starscriptParenthesisColor() {
        return starscriptParenthesis.get();
    }

    @Override
    public Color starscriptDotColor() {
        return starscriptDots.get();
    }

    @Override
    public Color starscriptCommaColor() {
        return starscriptCommas.get();
    }

    @Override
    public Color starscriptOperatorColor() {
        return starscriptOperators.get();
    }

    @Override
    public Color starscriptStringColor() {
        return starscriptStrings.get();
    }

    @Override
    public Color starscriptNumberColor() {
        return starscriptNumbers.get();
    }

    @Override
    public Color starscriptKeywordColor() {
        return starscriptKeywords.get();
    }

    @Override
    public Color starscriptAccessedObjectColor() {
        return starscriptAccessedObjects.get();
    }

    // Other

    @Override
    public TextRenderer textRenderer() {
        return TextRenderer.get();
    }

    @Override
    public double scale(double value) {
        double scaled = value * scale.get();

        if (MacWindowUtil.IS_MAC) {
            scaled /= (double) mc.getWindow().getWidth() / mc.getWindow().getFramebufferWidth();
        }

        return scaled;
    }

    @Override
    public boolean categoryIcons() {
        return categoryIcons.get();
    }

    @Override
    public boolean hideHUD() {
        return hideHUD.get();
    }

    public boolean roundedCorners() {
        return roundedCorners.get();
    }

    public double cornerRadius() {
        return scale(cornerRadius.get());
    }

    public boolean enableShadows() {
        return enableShadows.get();
    }

    public boolean smoothAnimations() {
        return smoothAnimations.get();
    }

    public SettingColor accentSecondaryColor() {
        return accentSecondaryColor.get();
    }

    public SettingColor glassEffectColor() {
        return glassEffectColor.get();
    }

    public class ThreeStateColorSetting {
        private final Setting<SettingColor> normal, hovered, pressed;

        public ThreeStateColorSetting(SettingGroup group, String name, SettingColor c1, SettingColor c2, SettingColor c3) {
            normal = color(group, name, "Color of " + name + ".", c1);
            hovered = color(group, "hovered-" + name, "Color of " + name + " when hovered.", c2);
            pressed = color(group, "pressed-" + name, "Color of " + name + " when pressed.", c3);
        }

        public SettingColor get() {
            return normal.get();
        }

        public SettingColor get(boolean pressed, boolean hovered, boolean bypassDisableHoverColor) {
            if (pressed) return this.pressed.get();
            return (hovered && (bypassDisableHoverColor || !disableHoverColor)) ? this.hovered.get() : this.normal.get();
        }

        public SettingColor get(boolean pressed, boolean hovered) {
            return get(pressed, hovered, false);
        }
    }
}
