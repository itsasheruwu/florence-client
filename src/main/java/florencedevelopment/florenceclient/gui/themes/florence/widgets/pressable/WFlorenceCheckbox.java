/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets.pressable;

import florencedevelopment.florenceclient.gui.animation.AnimatedFloat;
import florencedevelopment.florenceclient.gui.animation.Interaction;
import florencedevelopment.florenceclient.gui.animation.Spring;
import florencedevelopment.florenceclient.gui.design.Colors;
import florencedevelopment.florenceclient.gui.design.Design;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.pressable.WCheckbox;

public class WFlorenceCheckbox extends WCheckbox implements FlorenceWidget {
    private final Interaction ui = new Interaction();
    private final AnimatedFloat check;

    public WFlorenceCheckbox(boolean checked) {
        super(checked);
        check = new AnimatedFloat(checked ? 1 : 0);
    }

    @Override
    protected void onCalculateSize() {
        width = theme.textHeight() + theme().space(4);
        height = width;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        FlorenceGuiTheme theme = theme();
        Design d = design();

        ui.update(mouseOver, pressed, delta);

        check.springTo(checked ? 1 : 0, Spring.SNAPPY);
        double p = Math.max(0, Math.min(1, check.update(delta)));

        int fill = Colors.lerp(Colors.lerp(d.field, d.fieldHover, ui.hover()), Colors.lerp(d.accent, d.accentHover, ui.hover()), p);
        int border = Colors.lerp(Colors.lerp(d.outline, d.outlineHover, ui.hover()), d.accent, p);

        renderer.roundRect(x, y, width, height, theme.radiusSmall(), fill, lineWidth(), border);

        if (p > 0.01) {
            // Check mark
            double t = theme.scale(2);
            int color = Colors.mulAlpha(d.textOnAccent, p);

            renderer.line(x + width * 0.27, y + height * 0.52, x + width * 0.43, y + height * 0.68, t, color);
            renderer.line(x + width * 0.43, y + height * 0.68, x + width * 0.74, y + height * 0.34, t, color);
        }
    }
}
