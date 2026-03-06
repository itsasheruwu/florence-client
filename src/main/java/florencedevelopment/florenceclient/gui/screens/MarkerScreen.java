/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.screens;

import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.WindowScreen;
import florencedevelopment.florenceclient.gui.utils.Cell;
import florencedevelopment.florenceclient.gui.widgets.WWidget;
import florencedevelopment.florenceclient.gui.widgets.containers.WContainer;
import florencedevelopment.florenceclient.systems.modules.render.marker.BaseMarker;

public class MarkerScreen extends WindowScreen {
    private final BaseMarker marker;
    private WContainer settingsContainer;

    public MarkerScreen(GuiTheme theme, BaseMarker marker) {
        super(theme, marker.name.get());

        this.marker = marker;
    }

    @Override
    public void initWidgets() {
        // Settings
        if (!marker.settings.groups.isEmpty()) {
            settingsContainer = add(theme.verticalList()).expandX().widget();
            settingsContainer.add(theme.settings(marker.settings)).expandX();
        }

        // Custom widget
        WWidget widget = getWidget(theme);

        if (widget != null) {
            add(theme.horizontalSeparator()).expandX();
            Cell<WWidget> cell = add(widget);
            if (widget instanceof WContainer) cell.expandX();
        }
    }

    @Override
    public void tick() {
        super.tick();

        marker.settings.tick(settingsContainer, theme);
    }

    public WWidget getWidget(GuiTheme theme) {
        return null;
    }
}
