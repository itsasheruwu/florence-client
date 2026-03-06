/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.hud.elements;

import florencedevelopment.florenceclient.FlorenceClient;
import florencedevelopment.florenceclient.gui.GuiThemes;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.utils.StarscriptTextBoxRenderer;
import florencedevelopment.florenceclient.settings.*;
import florencedevelopment.florenceclient.systems.hud.Hud;
import florencedevelopment.florenceclient.systems.hud.HudElement;
import florencedevelopment.florenceclient.systems.hud.HudElementInfo;
import florencedevelopment.florenceclient.systems.hud.HudRenderer;
import florencedevelopment.florenceclient.utils.misc.FlorenceStarscript;
import florencedevelopment.florenceclient.utils.render.color.Color;
import florencedevelopment.florenceclient.utils.render.color.SettingColor;
import org.meteordev.starscript.Script;
import org.meteordev.starscript.Section;
import org.meteordev.starscript.compiler.Compiler;
import org.meteordev.starscript.compiler.Parser;
import org.meteordev.starscript.utils.StarscriptError;

import java.util.List;

public class TextHud extends HudElement {
    private static final Color WHITE = new Color();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgShown = settings.createGroup("Shown");
    private final SettingGroup sgScale = settings.createGroup("Scale");
    private final SettingGroup sgBackground = settings.createGroup("Background");

    private double originalWidth, originalHeight;
    private boolean needsCompile, recalculateSize;

    private int timer;

    // General

    public final Setting<String> text = sgGeneral.add(new StringSetting.Builder()
        .name("text")
        .description("Text to display with Starscript.")
        .defaultValue(FlorenceClient.NAME)
        .onChanged(s -> recompile())
        .wide()
        .renderer(StarscriptTextBoxRenderer.class)
        .build()
    );

    public final Setting<Integer> updateDelay = sgGeneral.add(new IntSetting.Builder()
        .name("update-delay")
        .description("Update delay in ticks")
        .defaultValue(4)
        .onChanged(integer -> {
            if (timer > integer) timer = integer;
        })
        .min(0)
        .build()
    );

    public final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Renders shadow behind text.")
        .defaultValue(true)
        .onChanged(aBoolean -> recalculateSize = true)
        .build()
    );

    public final Setting<Integer> border = sgGeneral.add(new IntSetting.Builder()
        .name("border")
        .description("How much space to add around the text.")
        .defaultValue(0)
        .onChanged(integer -> super.setSize(originalWidth + integer * 2, originalHeight + integer * 2))
        .build()
    );

    // Shown

    public final Setting<Shown> shown = sgShown.add(new EnumSetting.Builder<Shown>()
        .name("shown")
        .description("When this text element is shown.")
        .defaultValue(Shown.Always)
        .onChanged(s -> recompile())
        .build()
    );

    public final Setting<String> condition = sgShown.add(new StringSetting.Builder()
        .name("condition")
        .description("Condition to check when shown is not Always.")
        .visible(() -> shown.get() != Shown.Always)
        .onChanged(s -> recompile())
        .renderer(StarscriptTextBoxRenderer.class)
        .build()
    );

    // Scale

    public final Setting<Boolean> customScale = sgScale.add(new BoolSetting.Builder()
        .name("custom-scale")
        .description("Applies a custom scale to this hud element.")
        .defaultValue(false)
        .onChanged(aBoolean -> recalculateSize = true)
        .build()
    );

    public final Setting<Double> scale = sgScale.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Custom scale.")
        .visible(customScale::get)
        .defaultValue(1)
        .onChanged(aDouble -> recalculateSize = true)
        .min(0.5)
        .sliderRange(0.5, 3)
        .build()
    );

    // Background

    public final Setting<Boolean> background = sgBackground.add(new BoolSetting.Builder()
        .name("background")
        .description("Displays background.")
        .defaultValue(false)
        .build()
    );

    public final Setting<SettingColor> backgroundColor = sgBackground.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Color used for the background.")
        .visible(background::get)
        .defaultValue(new SettingColor(25, 25, 25, 50))
        .build()
    );

    private Script script, conditionScript;
    private Section section;

    private boolean firstTick = true;
    private boolean empty = false;
    private boolean visible;

    public TextHud(HudElementInfo<TextHud> info) {
        super(info);

        needsCompile = true;
    }

    private void recompile() {
        firstTick = true;
        needsCompile = true;
    }

    @Override
    public void setSize(double width, double height) {
        this.originalWidth = width;
        this.originalHeight = height;
        super.setSize(width + border.get() * 2, height + border.get() * 2);
    }

    private void calculateSize(HudRenderer renderer) {
        double width = 0;

        if (section != null) {
            String str = section.toString();
            if (!str.isBlank()) width = renderer.textWidth(str, shadow.get(), getScale());
        }

        if (width != 0) {
            setSize(width, renderer.textHeight(shadow.get(), getScale()));
            empty = false;
        }
        else {
            setSize(100, renderer.textHeight(shadow.get(), getScale()));
            empty = true;
        }
    }

    @Override
    public void tick(HudRenderer renderer) {
        if (recalculateSize) {
            calculateSize(renderer);
            recalculateSize = false;
        }

        if (timer <= 0) {
            runTick(renderer);
            timer = updateDelay.get();
        }
        else timer--;
    }

    private void runTick(HudRenderer renderer) {
        if (needsCompile) {
            Parser.Result result = Parser.parse(text.get());

            if (result.hasErrors()) {
                script = null;
                section = new Section(0, result.errors.getFirst().toString());
                calculateSize(renderer);
            }
            else script = Compiler.compile(result);

            if (shown.get() != Shown.Always) {
                conditionScript = Compiler.compile(Parser.parse(condition.get()));
            }

            needsCompile = false;
        }

        try {
            if (script != null) {
                section = FlorenceStarscript.ss.run(script);
                calculateSize(renderer);
            }
        }
        catch (StarscriptError error) {
            section = new Section(0, error.getMessage());
            calculateSize(renderer);
        }

        if (shown.get() != Shown.Always && conditionScript != null) {
            String text = FlorenceStarscript.run(conditionScript);
            if (text == null) visible = false;
            else visible = shown.get() == Shown.WhenTrue ? text.equalsIgnoreCase("true") : text.equalsIgnoreCase("false");
        }

        firstTick = false;
    }

    @Override
    public void render(HudRenderer renderer) {
        if (firstTick) runTick(renderer);

        boolean visible = shown.get() == Shown.Always || this.visible;

        if ((empty || !visible) && isInEditor()) {
            renderer.line(x, y, x + getWidth(), y + getHeight(), Color.GRAY);
            renderer.line(x, y + getHeight(), x + getWidth(), y, Color.GRAY);
        }

        if (section == null || !visible) return;

        double x = this.x + border.get();
        Section s = section;

        while (s != null) {
            String text = s.text;
            Color color = getSectionColor(s.index);
            boolean isBold = false;
            
            // Handle special markers: [accent] and [bold]
            if (text.contains("[accent]")) {
                color = getAccentColor();
                text = text.replace("[accent]", "");
            }
            if (text.contains("[bold]")) {
                isBold = true;
                text = text.replace("[bold]", "");
            }
            
            if (!text.isEmpty()) {
                if (isBold) {
                    // Render bold by rendering twice with slight offset for bold effect
                    renderer.text(text, x + 0.5, y + border.get(), color, shadow.get(), getScale());
                    x = renderer.text(text, x, y + border.get(), color, shadow.get(), getScale());
                } else {
                    x = renderer.text(text, x, y + border.get(), color, shadow.get(), getScale());
                }
            }
            s = s.next;
        }

        if (background.get()) {
            renderer.quad(this.x, y, getWidth(), getHeight(), backgroundColor.get());
        }
    }

    @Override
    public void onFontChanged() {
        recalculateSize = true;
    }

    private double getScale() {
        return customScale.get() ? scale.get() : Hud.get().getTextScale();
    }

    public static Color getSectionColor(int i) {
        List<SettingColor> colors = Hud.get().textColors.get();
        return (i >= 0 && i < colors.size()) ? colors.get(i) : WHITE;
    }

    private static Color getAccentColor() {
        try {
            var theme = GuiThemes.get();
            if (theme instanceof FlorenceGuiTheme florenceTheme) {
                return florenceTheme.accentColor.get();
            }
        } catch (Exception ignored) {}
        return WHITE;
    }

    public enum Shown {
        Always,
        WhenTrue,
        WhenFalse;

        @Override
        public String toString() {
            return switch (this) {
                case Always -> "Always";
                case WhenTrue -> "When True";
                case WhenFalse -> "When False";
            };
        }
    }
}
