/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.florence;

import florencedevelopment.florenceclient.events.Cancellable;
import florencedevelopment.florenceclient.utils.misc.input.KeyAction;
import net.minecraft.client.input.KeyInput;

public class KeyEvent extends Cancellable {
    private static final KeyEvent INSTANCE = new KeyEvent();

    public KeyInput input;
    public KeyAction action;

    public static KeyEvent get(KeyInput input, KeyAction action) {
        INSTANCE.setCancelled(false);
        INSTANCE.input = input;
        INSTANCE.action = action;
        return INSTANCE;
    }

    public int key() {
        return INSTANCE.input.key();
    }

    public int modifiers() {
        return INSTANCE.input.modifiers();
    }
}
