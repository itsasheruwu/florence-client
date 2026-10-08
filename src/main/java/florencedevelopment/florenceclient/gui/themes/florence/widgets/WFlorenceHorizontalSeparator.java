/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets;

import florencedevelopment.florenceclient.gui.design.Design;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.WHorizontalSeparator;

public class WFlorenceHorizontalSeparator extends WHorizontalSeparator implements FlorenceWidget {
    public WFlorenceHorizontalSeparator(String text) {
        super(text);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        FlorenceGuiTheme theme = theme();
        Design d = design();

        double thickness = lineWidth();
        double lineY = y + Math.round(height / 2.0) - thickness / 2;

        if (text == null) {
            renderer.roundRect(x, lineY, width, thickness, thickness / 2, d.divider);
            return;
        }

        // Small heading with a line running on from it
        double gap = theme.space(8);

        renderer.text(text, x, y, d.textSecondary, false);
        renderer.roundRect(x + textWidth + gap, lineY, Math.max(0, width - textWidth - gap), thickness, thickness / 2, d.divider);
    }
}
