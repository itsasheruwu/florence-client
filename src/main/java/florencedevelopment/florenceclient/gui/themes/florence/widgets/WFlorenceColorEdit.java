/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets;

import florencedevelopment.florenceclient.gui.animation.AnimatedFloat;
import florencedevelopment.florenceclient.gui.animation.Interaction;
import florencedevelopment.florenceclient.gui.animation.Spring;
import florencedevelopment.florenceclient.gui.design.Colors;
import florencedevelopment.florenceclient.gui.design.Design;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.WWidget;
import florencedevelopment.florenceclient.settings.ColorSetting;
import florencedevelopment.florenceclient.utils.render.color.SettingColor;
import net.minecraft.client.gui.Click;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

/**
 * Edits a color in place: a row with the color and its value that opens up to a square for saturation and brightness,
 * strips for hue and opacity and a switch for rainbow.
 */
public class WFlorenceColorEdit extends WWidget implements FlorenceWidget {
    private enum Drag { NONE, SQUARE, HUE, ALPHA }

    private final ColorSetting setting;

    private final Interaction ui = new Interaction();
    private final AnimatedFloat open = new AnimatedFloat(0);
    private final AnimatedFloat rainbowPosition = new AnimatedFloat(0);
    private double lastOpen;

    private double hue, saturation, value;
    private int alpha;
    private int lastPacked;
    private Drag dragging = Drag.NONE;

    public WFlorenceColorEdit(ColorSetting setting) {
        this.setting = setting;
        readColor();
        rainbowPosition.snap(setting.get().rainbow ? 1 : 0);
    }

    // Metrics

    private double headerHeight() {
        return theme.textHeight() + theme().space(8);
    }

    private double squareHeight() {
        return theme.scale(96);
    }

    private double stripHeight() {
        return theme.scale(12);
    }

    private double gap() {
        return theme().space(8);
    }

    private double rainbowHeight() {
        return theme.textHeight() + theme().space(4);
    }

    private double pickerHeight() {
        return gap() + squareHeight() + gap() + stripHeight() + gap() + stripHeight() + gap() + rainbowHeight() + theme().space(2);
    }

    @Override
    protected void onCalculateSize() {
        width = theme.scale(190);
        height = headerHeight() + pickerHeight() * open.get();
    }

    // Region tops, relative to the start of the picker
    private double squareY() {
        return y + headerHeight() + gap();
    }

    private double hueY() {
        return squareY() + squareHeight() + gap();
    }

    private double alphaY() {
        return hueY() + stripHeight() + gap();
    }

    private double rainbowY() {
        return alphaY() + stripHeight() + gap();
    }

    // Color

    private static int pack(SettingColor c) {
        return (c.a << 24) | (c.r << 16) | (c.g << 8) | c.b;
    }

    private void readColor() {
        SettingColor c = setting.get();
        int packed = pack(c);

        lastPacked = packed;
        alpha = c.a;

        double[] hsv = Colors.toHsv(packed);

        // Grey and black have no hue of their own, keep the one that was picked
        if (hsv[1] > 0 && hsv[2] > 0) hue = hsv[0];
        saturation = hsv[1];
        value = hsv[2];
    }

    private void writeColor() {
        int argb = Colors.fromHsv(hue, saturation, value, alpha);

        SettingColor c = setting.get();
        c.set(Colors.red(argb), Colors.green(argb), Colors.blue(argb), alpha);
        lastPacked = pack(c);

        setting.onChanged();
    }

    private String hex() {
        SettingColor c = setting.get();

        if (c.a < 255) return String.format("#%02X%02X%02X%02X", c.r, c.g, c.b, c.a);
        return String.format("#%02X%02X%02X", c.r, c.g, c.b);
    }

    // Input

    private boolean opened() {
        return open.get() > 0.95;
    }

    private static boolean inside(double mx, double my, double rx, double ry, double rw, double rh) {
        return mx >= rx && mx <= rx + rw && my >= ry && my <= ry + rh;
    }

    @Override
    public boolean onMouseClicked(Click click, boolean doubled) {
        if (!mouseOver || click.button() != GLFW_MOUSE_BUTTON_LEFT) return false;

        double mx = click.x();
        double my = click.y();

        if (my < y + headerHeight()) {
            open.animateTo(open.getTarget() > 0.5 ? 0 : 1, 0.24);
            relayout();
            return true;
        }

        if (!opened()) return false;

        if (inside(mx, my, x, squareY(), width, squareHeight())) dragging = Drag.SQUARE;
        else if (inside(mx, my, x, hueY(), width, stripHeight())) dragging = Drag.HUE;
        else if (inside(mx, my, x, alphaY(), width, stripHeight())) dragging = Drag.ALPHA;
        else if (inside(mx, my, x, rainbowY(), width, rainbowHeight())) {
            setting.get().rainbow = !setting.get().rainbow;
            setting.onChanged();
            return true;
        }

        if (dragging == Drag.NONE) return false;

        setFocused(true);
        drag(mx, my);
        return true;
    }

    @Override
    public void onMouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
        if (dragging != Drag.NONE) drag(mouseX, mouseY);
    }

    @Override
    public boolean onMouseReleased(Click click) {
        if (dragging == Drag.NONE) return false;

        dragging = Drag.NONE;
        setFocused(false);
        return true;
    }

    private void drag(double mx, double my) {
        double px = Math.max(0, Math.min(1, (mx - x) / width));

        switch (dragging) {
            case SQUARE -> {
                saturation = px;
                value = 1 - Math.max(0, Math.min(1, (my - squareY()) / squareHeight()));
            }
            case HUE -> hue = Math.min(px, 0.9999);
            case ALPHA -> alpha = (int) Math.round(px * 255);
            default -> {}
        }

        // Moving the color by hand ends the rainbow
        setting.get().rainbow = false;
        writeColor();
    }

    private void relayout() {
        WWidget widget = this;

        while (widget != null) {
            widget.invalidate();
            widget = widget.parent;
        }
    }

    // Rendering

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        FlorenceGuiTheme theme = theme();
        Design d = design();

        double amount = open.update(delta);

        if (amount != lastOpen) {
            lastOpen = amount;
            relayout();
        }

        // Follow the color when something else changes it, like the rainbow
        if (dragging == Drag.NONE && pack(setting.get()) != lastPacked) readColor();

        boolean headerHover = mouseOver && mouseY < y + headerHeight();
        ui.update(headerHover, false, delta);

        double th = theme.textHeight();
        double line = lineWidth();
        double radius = theme.radiusSmall();
        double headerH = headerHeight();

        // Header
        renderer.roundRect(x, y, width, headerH, theme.radiusMedium(), Colors.lerp(d.field, d.fieldHover, ui.hover()), line, Colors.lerp(d.outline, d.outlineHover, ui.hover()));

        SettingColor c = setting.get();
        int current = pack(c);
        double swatch = th * 0.8;
        double sy = y + (headerH - swatch) / 2;
        double sx = x + theme.space(8);

        renderer.roundRect(sx, sy, swatch, swatch, radius, d.trackOff);
        renderer.roundRect(sx, sy, swatch, swatch, radius, current, line, d.outlineHover);

        renderer.text(hex(), sx + swatch + theme.space(8), y + (headerH - th) / 2, d.text, false);

        chevron(renderer, x + width - theme.space(14), y + headerH / 2, th * 0.55, (amount - 1) * 90, Math.max(1.5, theme.scale(2)), Colors.lerp(d.textSecondary, d.text, ui.hover()));

        if (amount <= 0.001) return;

        // Picker, revealed from the top
        renderer.scissorStart(x - theme.scale(6), y + headerH, width + theme.scale(12), pickerHeight() * amount);

        double sqY = squareY();
        double sqH = squareHeight();
        int pureHue = Colors.fromHsv(hue, 1, 1, 255);

        // Saturation goes left to right and brightness from top to bottom
        renderer.roundRectHorizontal(x, sqY, width, sqH, radius, Colors.rgb(255, 255, 255), pureHue, 0, 0);
        renderer.roundRectVertical(x, sqY, width, sqH, radius, 0, Colors.argb(0, 0, 0, 255), line, d.outline);

        double hx = x + width * saturation;
        double hy = sqY + sqH * (1 - value);
        drawHandle(renderer, hx, hy, current | 0xFF000000, dragging == Drag.SQUARE);

        // Hue strip
        double stripH = stripHeight();
        double hY = hueY();
        double segment = width / 6.0;

        for (int i = 0; i < 6; i++) {
            renderer.roundRectHorizontal(x + segment * i, hY, segment + 0.5, stripH, 0, Colors.fromHsv(i / 6.0, 1, 1, 255), Colors.fromHsv((i + 1) / 6.0, 1, 1, 255), 0, 0);
        }

        renderer.roundRect(x, hY, width, stripH, radius, 0, line, d.outline);
        drawHandle(renderer, x + width * hue, hY + stripH / 2, Colors.fromHsv(hue, 1, 1, 255), dragging == Drag.HUE);

        // Opacity strip over a dark base
        double aY = alphaY();
        int opaque = Colors.fromHsv(hue, saturation, value, 255);

        renderer.roundRect(x, aY, width, stripH, radius, d.trackOff);
        renderer.roundRectHorizontal(x, aY, width, stripH, radius, Colors.withAlpha(opaque, 0), opaque, line, d.outline);
        drawHandle(renderer, x + width * (alpha / 255.0), aY + stripH / 2, current, dragging == Drag.ALPHA);

        // Rainbow
        double rY = rainbowY();
        double rH = rainbowHeight();
        boolean rainbow = c.rainbow;

        rainbowPosition.springTo(rainbow ? 1 : 0, Spring.SNAPPY);
        double rp = Math.max(0, Math.min(1, rainbowPosition.update(delta)));

        renderer.text("Rainbow", x, rY + (rH - th) / 2, d.textSecondary, false);

        double sh = th * 0.82;
        double sw = sh * 1.8;
        double swX = x + width - sw;
        double swY = rY + (rH - sh) / 2;
        double gapPx = Math.max(2, sh * 0.16);
        double knob = sh - gapPx * 2;

        renderer.roundRect(swX, swY, sw, sh, sh / 2, Colors.lerp(d.trackOff, d.accent, rp));
        renderer.roundRect(swX + gapPx + (sw - gapPx * 2 - knob) * rainbowPosition.get(), swY + gapPx, knob, knob, knob / 2, d.thumb);

        renderer.scissorEnd();
    }

    private void drawHandle(GuiRenderer renderer, double cx, double cy, int fill, boolean active) {
        FlorenceGuiTheme theme = theme();
        double r = theme.scale(active ? 7 : 6);
        double ring = Math.max(1.5, theme.scale(2));

        if (theme.shadows()) renderer.shadow(cx - r, cy - r + 1, r * 2, r * 2, r, theme.scale(4), Colors.argb(0, 0, 0, 120));

        renderer.roundRect(cx - r, cy - r, r * 2, r * 2, r, fill, ring, Colors.rgb(255, 255, 255));
    }
}
