/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.florence;

import florencedevelopment.florenceclient.events.Cancellable;
import florencedevelopment.florenceclient.utils.misc.input.KeyAction;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.MouseInput;

public class MouseClickEvent extends Cancellable {
    private static final MouseClickEvent INSTANCE = new MouseClickEvent();

    public Click click;
    public MouseInput input;
    public KeyAction action;

    public static MouseClickEvent get(Click click, KeyAction action) {
        INSTANCE.setCancelled(false);
        INSTANCE.click = click;
        INSTANCE.input = click.buttonInfo();
        INSTANCE.action = action;
        return INSTANCE;
    }

    public int button() {
        return INSTANCE.input.button();
    }
}
