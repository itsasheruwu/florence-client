/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.widgets.pressable;

import florencedevelopment.florenceclient.gui.renderer.packer.GuiTexture;
import net.minecraft.client.gui.Click;

public abstract class WConfirmedButton extends WButton {

    protected boolean pressedOnce = false;
    protected String confirmText;

    public WConfirmedButton(String text, String confirmText, GuiTexture texture) {
        super(text, texture);
        this.confirmText = confirmText;
    }

    @Override
    public boolean onMouseClicked(Click click, boolean doubled) {
        boolean pressed = super.onMouseClicked(click, doubled);
        if (!pressed) {
            pressedOnce = false;
            invalidate();
        }
        return pressed;
    }

    @Override
    public boolean onMouseReleased(Click click) {
        if (pressed && pressedOnce) super.onMouseReleased(click);
        pressedOnce = pressed;
        invalidate();
        return pressed = false;
    }

    @Override
    public String getText() {
        return pressedOnce ? confirmText : text;
    }

    public void set(String text, String confirmText) {
        super.set(text);
        this.confirmText = confirmText;
    }
}
