/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets.input;

import florencedevelopment.florenceclient.gui.animation.AnimatedFloat;
import florencedevelopment.florenceclient.gui.animation.Interaction;
import florencedevelopment.florenceclient.gui.design.Colors;
import florencedevelopment.florenceclient.gui.design.Design;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.themes.florence.widgets.WFlorenceLabel;
import florencedevelopment.florenceclient.gui.utils.CharFilter;
import florencedevelopment.florenceclient.gui.widgets.WWidget;
import florencedevelopment.florenceclient.gui.widgets.containers.WContainer;
import florencedevelopment.florenceclient.gui.widgets.containers.WVerticalList;
import florencedevelopment.florenceclient.gui.widgets.input.WTextBox;

public class WFlorenceTextBox extends WTextBox implements FlorenceWidget {
    private final Interaction ui = new Interaction();
    private final AnimatedFloat focus = new AnimatedFloat(0);

    private boolean cursorVisible;
    private double cursorTimer;

    private double cursorAlpha;

    public WFlorenceTextBox(String text, String placeholder, CharFilter filter, Class<? extends Renderer> renderer) {
        super(text, placeholder, filter, renderer);
    }

    @Override
    protected WContainer createCompletionsRootWidget() {
        return new WVerticalList() {
            @Override
            protected void onRender(GuiRenderer renderer1, double mouseX, double mouseY, double delta) {
                FlorenceGuiTheme theme1 = theme();
                Design d = design();

                if (theme1.shadows()) renderer1.shadow(x, y + 2, width, height, theme1.radiusMedium(), theme1.scale(12), d.shadow);
                renderer1.roundRect(x, y, width, height, theme1.radiusMedium(), Colors.withAlpha(d.header, 252), lineWidth(), d.outlineHover);
            }

            private FlorenceGuiTheme theme() {
                return (FlorenceGuiTheme) theme;
            }

            private Design design() {
                return theme().design();
            }

            private double lineWidth() {
                return Math.max(1, Math.round(theme().scale(1)));
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T extends WWidget & ICompletionItem> T createCompletionsValueWidth(String completion, boolean selected) {
        return (T) new CompletionItem(completion, false, selected);
    }

    private static class CompletionItem extends WFlorenceLabel implements ICompletionItem {
        private boolean selected;

        public CompletionItem(String text, boolean title, boolean selected) {
            super(text, title);
            this.selected = selected;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            if (selected) {
                FlorenceGuiTheme theme = theme();
                renderer.roundRect(x - theme.space(4), y - theme.space(2), width + theme.space(8), height + theme.space(4), theme.radiusSmall(), design().accentSoft);
            }

            super.onRender(renderer, mouseX, mouseY, delta);
        }

        @Override
        public boolean isSelected() {
            return selected;
        }

        @Override
        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        @Override
        public String getCompletion() {
            return text;
        }
    }

    private int lastLength = -1;

    @Override
    protected void onCalculateSize() {
        super.onCalculateSize();

        // As wide as what is in it, with room for a few more characters. Text boxes that are stretched or have a
        // minimum width are not affected by this.
        String shown = text.isEmpty() ? "000" : text;
        width = pad() * 2 + theme.textWidth(shown) + theme.scale(4);
    }

    @Override
    protected void onCursorChanged() {
        cursorVisible = true;
        cursorTimer = 0;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        FlorenceGuiTheme theme = theme();
        Design d = design();

        if (cursorTimer >= 1) {
            cursorVisible = !cursorVisible;
            cursorTimer = 0;
        }
        else {
            cursorTimer += delta * 1.75;
        }

        // Resize when the text gets longer or shorter
        if (text.length() != lastLength) {
            if (lastLength != -1) invalidate();
            lastLength = text.length();
        }

        ui.update(mouseOver, false, delta);
        focus.animateTo(focused ? 1 : 0, 0.15);
        double f = focus.update(delta);

        // Field
        int fill = Colors.lerp(Colors.lerp(d.field, d.fieldHover, ui.hover()), d.fieldFocus, f);
        int border = Colors.lerp(Colors.lerp(d.outline, d.outlineHover, ui.hover()), d.accent, f);

        // Soft ring around the field while it has focus
        if (f > 0.01) {
            double ring = Math.max(2, theme.scale(2.5));
            renderer.roundRect(x - ring, y - ring, width + ring * 2, height + ring * 2, theme.radiusMedium() + ring, 0, ring, Colors.mulAlpha(d.accent, 0.3 * f));
        }

        renderer.roundRect(x, y, width, height, theme.radiusMedium(), fill, lineWidth(), border);

        double pad = pad();
        double overflowWidth = getOverflowWidthForRender();
        double textY = y + (height - theme.textHeight()) / 2;

        renderer.scissorStart(x + pad, y, width - pad * 2, height);

        // Text content
        if (!text.isEmpty()) {
            this.renderer.render(renderer, x + pad - overflowWidth, textY, text, theme.textColor());
        }
        else if (placeholder != null) {
            renderer.text(placeholder, x + pad - overflowWidth, textY, d.textDisabled, false);
        }

        // Text highlighting
        if (focused && (cursor != selectionStart || cursor != selectionEnd)) {
            double selStart = x + pad + getTextWidth(selectionStart) - overflowWidth;
            double selEnd = x + pad + getTextWidth(selectionEnd) - overflowWidth;

            renderer.roundRect(selStart, textY, selEnd - selStart, theme.textHeight(), theme.radiusSmall() * 0.5, Colors.withAlpha(d.accent, 90));
        }

        // Cursor
        cursorAlpha += delta * 10 * (focused && cursorVisible ? 1 : -1);
        cursorAlpha = Math.max(0, Math.min(1, cursorAlpha));

        if ((focused && cursorVisible) || cursorAlpha > 0) {
            renderer.line(
                x + pad + getTextWidth(cursor) - overflowWidth, textY,
                x + pad + getTextWidth(cursor) - overflowWidth, textY + theme.textHeight(),
                Math.max(1, theme.scale(1.5)), Colors.mulAlpha(d.text, cursorAlpha)
            );
        }

        renderer.scissorEnd();
    }
}
