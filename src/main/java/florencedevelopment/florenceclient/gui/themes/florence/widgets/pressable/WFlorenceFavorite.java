/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets.pressable;

import florencedevelopment.florenceclient.gui.animation.Interaction;
import florencedevelopment.florenceclient.gui.design.Colors;
import florencedevelopment.florenceclient.gui.design.Design;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.pressable.WFavorite;
import florencedevelopment.florenceclient.utils.render.color.Color;

public class WFlorenceFavorite extends WFavorite implements FlorenceWidget {
    private final Interaction ui = new Interaction();

    public WFlorenceFavorite(boolean checked) {
        super(checked);
    }

    @Override
    protected void onCalculateSize() {
        width = theme.textHeight() + theme().space(6);
        height = width;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        Design d = design();

        ui.update(mouseOver, pressed, delta);

        int color = checked
            ? Colors.lerp(d.favorite, Colors.lighten(d.favorite, 0.3), ui.hover())
            : Colors.lerp(d.textDisabled, d.textSecondary, ui.hover());

        double s = theme.textHeight();
        renderer.icon(checked ? GuiRenderer.FAVORITE_YES : GuiRenderer.FAVORITE_NO, x + (width - s) / 2, y + (height - s) / 2, s, s, color);
    }

    @Override
    protected Color getColor() {
        return theme().textColor();
    }
}
