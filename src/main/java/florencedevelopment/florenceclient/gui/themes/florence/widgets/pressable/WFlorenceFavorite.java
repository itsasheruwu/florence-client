/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets.pressable;

import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.pressable.WFavorite;
import florencedevelopment.florenceclient.utils.render.color.Color;

public class WFlorenceFavorite extends WFavorite implements FlorenceWidget {
    public WFlorenceFavorite(boolean checked) {
        super(checked);
    }

    @Override
    protected Color getColor() {
        return theme().favoriteColor.get();
    }
}
