/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.gui;

import florencedevelopment.florenceclient.gui.WidgetScreen;

/**
 * Lifecycle of the GUI screens. The three events are posted in order: opened when the screen is shown, closing when it
 * starts to fade out and closed once it is gone.
 */
public class GuiScreenEvent {
    public static class Opened extends GuiScreenEvent {
        private static final Opened INSTANCE = new Opened();

        public static Opened get(WidgetScreen screen) {
            INSTANCE.screen = screen;
            return INSTANCE;
        }
    }

    public static class Closing extends GuiScreenEvent {
        private static final Closing INSTANCE = new Closing();

        public static Closing get(WidgetScreen screen) {
            INSTANCE.screen = screen;
            return INSTANCE;
        }
    }

    public static class Closed extends GuiScreenEvent {
        private static final Closed INSTANCE = new Closed();

        public static Closed get(WidgetScreen screen) {
            INSTANCE.screen = screen;
            return INSTANCE;
        }
    }

    public WidgetScreen screen;
}
