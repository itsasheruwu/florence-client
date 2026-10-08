/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.gui;

import florencedevelopment.florenceclient.systems.modules.Module;

/**
 * Posted after a module has been turned on or off.
 */
public class ModuleToggledEvent {
    private static final ModuleToggledEvent INSTANCE = new ModuleToggledEvent();

    public Module module;
    /** The state of the module after the toggle. */
    public boolean active;
    public long timeNanos;

    public static ModuleToggledEvent get(Module module, boolean active, long timeNanos) {
        INSTANCE.module = module;
        INSTANCE.active = active;
        INSTANCE.timeNanos = timeNanos;
        return INSTANCE;
    }
}
