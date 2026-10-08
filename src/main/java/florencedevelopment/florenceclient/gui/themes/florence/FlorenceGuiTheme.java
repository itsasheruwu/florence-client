/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence;

import florencedevelopment.florenceclient.gui.DefaultSettingsWidgetFactory;
import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.WidgetScreen;
import florencedevelopment.florenceclient.gui.animation.Motion;
import florencedevelopment.florenceclient.gui.design.Design;
import florencedevelopment.florenceclient.gui.design.Preset;
import florencedevelopment.florenceclient.gui.renderer.packer.GuiTexture;
import florencedevelopment.florenceclient.gui.themes.florence.widgets.*;
import florencedevelopment.florenceclient.gui.themes.florence.widgets.input.WFlorenceDropdown;
import florencedevelopment.florenceclient.gui.themes.florence.widgets.input.WFlorenceSlider;
import florencedevelopment.florenceclient.gui.themes.florence.widgets.input.WFlorenceTextBox;
import florencedevelopment.florenceclient.gui.themes.florence.widgets.pressable.*;
import florencedevelopment.florenceclient.gui.utils.CharFilter;
import florencedevelopment.florenceclient.gui.widgets.*;
import florencedevelopment.florenceclient.gui.widgets.containers.WSection;
import florencedevelopment.florenceclient.gui.widgets.containers.WView;
import florencedevelopment.florenceclient.gui.widgets.containers.WWindow;
import florencedevelopment.florenceclient.gui.widgets.input.WDropdown;
import florencedevelopment.florenceclient.gui.widgets.input.WSlider;
import florencedevelopment.florenceclient.gui.widgets.input.WTextBox;
import florencedevelopment.florenceclient.gui.widgets.pressable.*;
import florencedevelopment.florenceclient.renderer.BackdropBlur;
import florencedevelopment.florenceclient.renderer.text.TextRenderer;
import florencedevelopment.florenceclient.settings.*;
import florencedevelopment.florenceclient.systems.accounts.Account;
import florencedevelopment.florenceclient.systems.modules.Module;
import florencedevelopment.florenceclient.utils.misc.Keybind;
import florencedevelopment.florenceclient.utils.render.color.Color;
import florencedevelopment.florenceclient.utils.render.color.SettingColor;
import net.minecraft.client.util.MacWindowUtil;

import static florencedevelopment.florenceclient.FlorenceClient.mc;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT;

public class FlorenceGuiTheme extends GuiTheme {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgEffects = settings.createGroup("Effects");
    private final SettingGroup sgWindows = settings.createGroup("Windows");
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

    public final Setting<Preset> preset = sgGeneral.add(new EnumSetting.Builder<Preset>()
        .name("preset")
        .description("The base look of the GUI.")
        .defaultValue(Preset.DARK)
        .onChanged(v -> rebuildDesign())
        .build()
    );

    public final Setting<SettingColor> accentColor = sgGeneral.add(new ColorSetting.Builder()
        .name("accent")
        .description("Main color of the GUI.")
        .defaultValue(new SettingColor(108, 92, 231))
        .onChanged(v -> rebuildDesign())
        .build()
    );

    public final Setting<SettingColor> accentSecondaryColor = sgGeneral.add(new ColorSetting.Builder()
        .name("accent-secondary")
        .description("Second color of gradients.")
        .defaultValue(new SettingColor(74, 144, 255))
        .onChanged(v -> rebuildDesign())
        .build()
    );

    public final Setting<Density> density = sgGeneral.add(new EnumSetting.Builder<Density>()
        .name("density")
        .description("How much room there is between things.")
        .defaultValue(Density.Comfortable)
        .onChanged(v -> {
            if (mc.currentScreen instanceof WidgetScreen) ((WidgetScreen) mc.currentScreen).invalidate();
        })
        .build()
    );

    public final Setting<Double> categoryWidth = sgGeneral.add(new DoubleSetting.Builder()
        .name("window-width")
        .description("Width of the module windows.")
        .defaultValue(300)
        .range(180, 520)
        .sliderRange(180, 520)
        .decimalPlaces(0)
        .onChanged(v -> {
            if (mc.currentScreen instanceof florencedevelopment.florenceclient.gui.screens.ModulesScreen screen) screen.reload();
        })
        .build()
    );

    @Override
    public double windowWidth() {
        return categoryWidth.get();
    }

    public final Setting<Double> cornerRadius = sgGeneral.add(new DoubleSetting.Builder()
        .name("corner-radius")
        .description("How round the corners of windows are, the corners of buttons and fields follow.")
        .defaultValue(10)
        .min(0)
        .max(24)
        .sliderRange(0, 20)
        .build()
    );

    public final Setting<Boolean> categoryIcons = sgGeneral.add(new BoolSetting.Builder()
        .name("category-icons")
        .description("Adds item icons to module categories.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> notifications = sgGeneral.add(new BoolSetting.Builder()
        .name("notifications")
        .description("Shows a small message when a module is turned on or off or bound to a key from the GUI.")
        .defaultValue(true)
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

    // Effects

    public final Setting<Boolean> glass = sgEffects.add(new BoolSetting.Builder()
        .name("glass")
        .description("Shows the blurred world through windows.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Integer> blurStrength = sgEffects.add(new IntSetting.Builder()
        .name("blur-strength")
        .description("How blurred the world behind windows is.")
        .defaultValue(8)
        .range(1, BackdropBlur.MAX_LEVEL)
        .sliderRange(1, BackdropBlur.MAX_LEVEL)
        .visible(glass::get)
        .build()
    );

    public final Setting<Double> panelOpacity = sgEffects.add(new DoubleSetting.Builder()
        .name("panel-opacity")
        .description("How opaque windows are. Lower values show more of the world behind them.")
        .defaultValue(0.78)
        .range(0.3, 1)
        .sliderRange(0.3, 1)
        .onChanged(v -> rebuildDesign())
        .build()
    );

    public final Setting<Boolean> shadows = sgEffects.add(new BoolSetting.Builder()
        .name("shadows")
        .description("Draws soft shadows below windows and menus.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Double> animationSpeed = sgEffects.add(new DoubleSetting.Builder()
        .name("animation-speed")
        .description("How fast animations play.")
        .defaultValue(1)
        .range(0.5, 2.5)
        .sliderRange(0.5, 2.5)
        .onChanged(v -> configureMotion())
        .build()
    );

    public final Setting<Boolean> reducedMotion = sgEffects.add(new BoolSetting.Builder()
        .name("reduced-motion")
        .description("Skips animations.")
        .defaultValue(false)
        .onChanged(v -> configureMotion())
        .build()
    );

    // Windows

    public final Setting<Boolean> snapToGrid = sgWindows.add(new BoolSetting.Builder()
        .name("snap-to-grid")
        .description("Snap click GUI windows to a grid while dragging.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Integer> gridSize = sgWindows.add(new IntSetting.Builder()
        .name("grid-size")
        .description("Spacing between click GUI grid lines.")
        .defaultValue(16)
        .range(8, 64)
        .sliderRange(8, 64)
        .visible(snapToGrid::get)
        .build()
    );

    public final Setting<Double> gridSnapSmoothness = sgWindows.add(new DoubleSetting.Builder()
        .name("grid-snap-smoothness")
        .description("How strongly windows are pulled toward nearby grid points while dragging.")
        .defaultValue(0.35)
        .range(0.1, 1.0)
        .sliderRange(0.1, 1.0)
        .visible(snapToGrid::get)
        .build()
    );

    public final Setting<Keybind> resizeWindowKeybind = sgWindows.add(new KeybindSetting.Builder()
        .name("resize-window-key")
        .description("Hold this key to resize click GUI windows by dragging their borders.")
        .defaultValue(Keybind.fromKey(GLFW_KEY_LEFT_ALT))
        .build()
    );

    public final Setting<Integer> gridOpacity = sgWindows.add(new IntSetting.Builder()
        .name("grid-opacity")
        .description("Opacity of the click GUI snap grid.")
        .defaultValue(24)
        .range(0, 255)
        .sliderRange(0, 255)
        .visible(snapToGrid::get)
        .build()
    );

    public final Setting<SettingColor> gridColor = sgWindows.add(new ColorSetting.Builder()
        .name("grid-color")
        .description("Color of the click GUI snap grid.")
        .defaultValue(new SettingColor(150, 160, 255))
        .visible(snapToGrid::get)
        .build()
    );

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

    // Derived from the settings above
    private Design design;
    private int designAccent;
    private final Color textColor = new Color();
    private final Color textSecondaryColor = new Color();

    public FlorenceGuiTheme() {
        super("Florence");

        settingsFactory = new DefaultSettingsWidgetFactory(this);

        rebuildDesign();
        configureMotion();
    }

    private Setting<SettingColor> color(SettingGroup group, String name, String description, SettingColor color) {
        return group.add(new ColorSetting.Builder()
                .name(name + "-color")
                .description(description)
                .defaultValue(color)
                .build());
    }

    // Design

    /**
     * The colors everything is drawn with.
     */
    public Design design() {
        // The accent can be a rainbow, which changes the color without a setting being changed
        if (packed(accentColor.get()) != designAccent) rebuildDesign();

        return design;
    }

    private void rebuildDesign() {
        SettingColor accent = accentColor.get();
        SettingColor accent2 = accentSecondaryColor.get();

        designAccent = packed(accent);
        design = Design.build(preset.get(), designAccent, packed(accent2), panelOpacity.get());

        textColor.set(toColor(design.text));
        textSecondaryColor.set(toColor(design.textSecondary));
    }

    private void configureMotion() {
        Motion.configure(animationSpeed.get(), reducedMotion.get());
    }

    private static int packed(Color color) {
        return (color.a << 24) | (color.r << 16) | (color.g << 8) | color.b;
    }

    /**
     * Converts a color packed as 0xAARRGGBB to a {@link Color}, for the places that still need one.
     */
    public static Color toColor(int argb) {
        return new Color((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, argb >>> 24);
    }

    @Override
    public double pad() {
        return space(7);
    }

    // Sizes

    /**
     * A distance in the units the GUI is laid out in, scaled by the GUI scale and the density.
     */
    public double space(double value) {
        return scale(value * density.get().factor);
    }

    /** Corner radius of windows and menus. */
    public double radiusLarge() {
        return scale(cornerRadius.get());
    }

    /** Corner radius of buttons, fields and rows. */
    public double radiusMedium() {
        return scale(cornerRadius.get() * 0.6);
    }

    /** Corner radius of small things like checkboxes and keybind chips. */
    public double radiusSmall() {
        return scale(cornerRadius.get() * 0.35);
    }

    public boolean shadows() {
        return shadows.get();
    }

    public boolean glass() {
        return glass.get();
    }

    @Override
    public int backdropBlurLevel() {
        return glass.get() ? blurStrength.get() : 0;
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
    public WCheckbox toggle(boolean checked) {
        return w(new WFlorenceSwitch(checked));
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
        design();
        return textColor;
    }

    @Override
    public Color textSecondaryColor() {
        design();
        return textSecondaryColor;
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

    public boolean snapToGrid() {
        return snapToGrid.get();
    }

    public double gridSizePixels() {
        return Math.max(8, Math.round(scale(gridSize.get())));
    }

    public double gridSnapSmoothness() {
        return gridSnapSmoothness.get();
    }

    public int gridOpacity() {
        return gridOpacity.get();
    }

    public SettingColor gridColor() {
        return gridColor.get();
    }

    public Keybind resizeWindowKeybind() {
        return resizeWindowKeybind.get();
    }

    public enum Density {
        Compact(0.85),
        Comfortable(1),
        Spacious(1.2);

        private final double factor;

        Density(double factor) {
            this.factor = factor;
        }
    }
}
