/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets.pressable;

import florencedevelopment.florenceclient.gui.animation.Interaction;
import florencedevelopment.florenceclient.gui.design.Colors;
import florencedevelopment.florenceclient.gui.design.Design;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.renderer.packer.GuiTexture;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.pressable.WConfirmedButton;

public class WFlorenceConfirmedButton extends WConfirmedButton implements FlorenceWidget {
    private final Interaction ui = new Interaction();

    public WFlorenceConfirmedButton(String text, String confirmText, GuiTexture texture) {
        super(text, confirmText, texture);
    }

    @Override
    protected void onCalculateSize() {
        FlorenceGuiTheme theme = theme();
        double padX = theme.space(10);
        double padY = theme.space(6);

        String text = getText();

        if (text != null) {
            textWidth = theme.textWidth(text);

            width = padX + textWidth + padX;
            height = padY + theme.textHeight() + padY;
        }
        else {
            double s = theme.textHeight();

            width = padY + s + padY;
            height = width;
        }
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        FlorenceGuiTheme theme = theme();
        Design d = design();

        ui.update(mouseOver, pressed, delta);

        int color;

        if (pressedOnce) {
            // Waiting for the second click
            int fill = Colors.lerp(Colors.withAlpha(d.danger, 200), d.danger, ui.hover());
            renderer.roundRect(x, y, width, height, theme.radiusMedium(), fill, lineWidth(), d.danger);
            color = Colors.rgb(255, 255, 255);
        }
        else {
            renderControl(renderer, this, ui);
            color = controlTextColor(ui);
        }

        String text = getText();

        if (text != null) {
            renderer.text(text, x + width / 2 - textWidth / 2, y + (height - theme.textHeight()) / 2, color, false);
        }
        else {
            double ts = theme.textHeight();
            renderer.icon(texture, x + (width - ts) / 2, y + (height - ts) / 2, ts, ts, color);
        }
    }
}
