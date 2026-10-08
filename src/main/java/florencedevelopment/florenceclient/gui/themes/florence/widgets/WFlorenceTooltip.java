/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets;

import florencedevelopment.florenceclient.gui.design.Colors;
import florencedevelopment.florenceclient.gui.design.Design;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.WTooltip;

public class WFlorenceTooltip extends WTooltip implements FlorenceWidget {
    public WFlorenceTooltip(String text) {
        super(text);
    }

    @Override
    public void init() {
        add(theme.label(text)).pad(7);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        FlorenceGuiTheme theme = theme();
        Design d = design();

        if (theme.shadows()) renderer.shadow(x, y + 2, width, height, theme.radiusMedium(), theme.scale(14), d.shadow);

        renderer.roundRect(x, y, width, height, theme.radiusMedium(), Colors.withAlpha(d.header, 250), lineWidth(), d.outlineHover);
    }
}
