/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.game;

import florencedevelopment.florenceclient.events.Cancellable;
import net.minecraft.client.gui.screen.Screen;

public class OpenScreenEvent extends Cancellable {
    private static final OpenScreenEvent INSTANCE = new OpenScreenEvent();

    public Screen screen;

    public static OpenScreenEvent get(Screen screen) {
        INSTANCE.setCancelled(false);
        INSTANCE.screen = screen;
        return INSTANCE;
    }
}
