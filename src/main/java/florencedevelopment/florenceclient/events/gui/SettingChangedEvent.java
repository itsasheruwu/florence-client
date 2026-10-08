/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.gui;

import florencedevelopment.florenceclient.settings.Setting;
import florencedevelopment.florenceclient.systems.modules.Module;

/**
 * Posted whenever the value of a setting changes, including edits made in place to a list or a color.
 */
public class SettingChangedEvent {
    private static final SettingChangedEvent INSTANCE = new SettingChangedEvent();

    public enum Source {
        /** Changed by the user or by code. */
        USER,
        /** Read from a saved config. */
        LOAD
    }

    public Setting<?> setting;
    /** The module the setting belongs to, null for settings of the GUI, the HUD and other systems. */
    public Module module;
    public Source source;

    public static SettingChangedEvent get(Setting<?> setting, Source source) {
        INSTANCE.setting = setting;
        INSTANCE.module = setting.module;
        INSTANCE.source = source;
        return INSTANCE;
    }
}
