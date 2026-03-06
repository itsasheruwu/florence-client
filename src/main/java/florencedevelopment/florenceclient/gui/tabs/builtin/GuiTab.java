/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.tabs.builtin;

import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.GuiThemes;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.tabs.Tab;
import florencedevelopment.florenceclient.gui.tabs.TabScreen;
import florencedevelopment.florenceclient.gui.tabs.WindowTabScreen;
import florencedevelopment.florenceclient.gui.widgets.containers.WHorizontalList;
import florencedevelopment.florenceclient.gui.widgets.input.WDropdown;
import florencedevelopment.florenceclient.gui.widgets.pressable.WButton;
import florencedevelopment.florenceclient.utils.misc.NbtUtils;
import net.minecraft.client.gui.screen.Screen;

import static florencedevelopment.florenceclient.FlorenceClient.mc;

public class GuiTab extends Tab {
    public GuiTab() {
        super("GUI");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new GuiScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof GuiScreen;
    }

    private static class GuiScreen extends WindowTabScreen {
        public GuiScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);

            theme.settings.onActivated();
        }

        @Override
        public void initWidgets() {
            WHorizontalList opts = add(theme.horizontalList()).expandX().widget();

            opts.add(theme.label("Theme:"));
            WDropdown<String> themeW = opts.add(theme.dropdown(GuiThemes.getNames(), GuiThemes.get().name)).widget();
            themeW.action = () -> {
                GuiThemes.select(themeW.get());

                mc.setScreen(null);
                tab.openScreen(GuiThemes.get());
            };

            WButton resetLayout = opts.add(theme.button("Reset Layout")).expandX().widget();
            resetLayout.action = theme::clearWindowConfigs;

            WButton reset = opts.add(theme.button("Reset Colors")).right().widget();
            reset.action = () -> {
                theme.settings.reset();
                mc.setScreen(null);
                tab.openScreen(GuiThemes.get());
            };

            WButton copyButton = opts.add(theme.button(GuiRenderer.COPY)).widget();
            copyButton.action = this::toClipboard;
            copyButton.tooltip = "Copy config";

            WButton pasteButton = opts.add(theme.button(GuiRenderer.PASTE)).right().widget();
            pasteButton.action = this::fromClipboard;
            pasteButton.tooltip = "Paste config";

            add(theme.settings(theme.settings)).expandX();
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard(theme);
        }

        @Override
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(theme);
        }
    }
}
