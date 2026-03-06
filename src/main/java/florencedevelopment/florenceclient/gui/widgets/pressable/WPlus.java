/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.widgets.pressable;

public abstract class WPlus extends WPressable {
    @Override
    protected void onCalculateSize() {
        double pad = pad();
        double s = theme.textHeight();

        width = pad + s + pad;
        height = pad + s + pad;
    }
}
