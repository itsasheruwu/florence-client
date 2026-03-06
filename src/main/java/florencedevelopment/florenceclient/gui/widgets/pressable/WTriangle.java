/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.widgets.pressable;

public abstract class WTriangle extends WPressable {
    public double rotation;

    @Override
    protected void onCalculateSize() {
        double s = theme.textHeight();

        width = s;
        height = s;
    }
}
