/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.tabs.builtin;

import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.tabs.Tab;
import florencedevelopment.florenceclient.gui.tabs.TabScreen;
import florencedevelopment.florenceclient.gui.tabs.WindowTabScreen;
import florencedevelopment.florenceclient.gui.widgets.containers.WContainer;
import florencedevelopment.florenceclient.gui.widgets.containers.WHorizontalList;
import florencedevelopment.florenceclient.gui.widgets.pressable.WButton;
import florencedevelopment.florenceclient.gui.widgets.pressable.WCheckbox;
import florencedevelopment.florenceclient.systems.hud.Hud;
import florencedevelopment.florenceclient.systems.hud.screens.HudEditorScreen;
import florencedevelopment.florenceclient.utils.misc.NbtUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import static florencedevelopment.florenceclient.FlorenceClient.mc;

public class HudTab extends Tab {
    public HudTab() {
        super("HUD");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new HudScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof HudScreen;
    }

    public static class HudScreen extends WindowTabScreen {
        private WContainer settingsContainer;
        private final Hud hud;

        public HudScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);

            hud = Hud.get();
            hud.settings.onActivated();
        }

        @Override
        public void initWidgets() {
            settingsContainer = add(theme.verticalList()).expandX().widget();
            settingsContainer.add(theme.settings(hud.settings)).expandX().widget();

            add(theme.horizontalSeparator()).expandX();

            WButton openEditor = add(theme.button("Edit")).expandX().widget();
            openEditor.action = () -> mc.setScreen(new HudEditorScreen(theme));

            WHorizontalList buttons = add(theme.horizontalList()).expandX().widget();
            buttons.add(theme.confirmedButton("Clear", "Confirm")).expandX().widget().action = hud::clear;
            buttons.add(theme.confirmedButton("Reset to default elements", "Confirm")).expandX().widget().action = hud::resetToDefaultElements;

            add(theme.horizontalSeparator()).expandX();

            WHorizontalList bottom = add(theme.horizontalList()).expandX().widget();

            bottom.add(theme.label("Active: "));
            WCheckbox active = bottom.add(theme.checkbox(hud.active)).expandCellX().widget();
            active.action = () -> hud.active = active.checked;

            WButton resetSettings = bottom.add(theme.button(GuiRenderer.RESET)).widget();
            resetSettings.action = hud.settings::reset;
            resetSettings.tooltip = "Reset";
        }

        @Override
        protected void onRenderBefore(DrawContext drawContext, float delta) {
            HudEditorScreen.renderElements(drawContext);
        }

        @Override
        public void tick() {
            super.tick();

            hud.settings.tick(settingsContainer, theme);
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard(hud);
        }

        @Override
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(hud);
        }
    }
}
