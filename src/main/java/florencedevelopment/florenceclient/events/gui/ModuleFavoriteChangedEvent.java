/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.gui;

import florencedevelopment.florenceclient.systems.modules.Module;

public class ModuleFavoriteChangedEvent {
    private static final ModuleFavoriteChangedEvent INSTANCE = new ModuleFavoriteChangedEvent();

    public Module module;
    public boolean favorite;

    public static ModuleFavoriteChangedEvent get(Module module, boolean favorite) {
        INSTANCE.module = module;
        INSTANCE.favorite = favorite;
        return INSTANCE;
    }
}
