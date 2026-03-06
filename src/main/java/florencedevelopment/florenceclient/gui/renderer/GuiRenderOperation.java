/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.renderer;

import florencedevelopment.florenceclient.utils.misc.Pool;
import florencedevelopment.florenceclient.utils.render.color.Color;

public abstract class GuiRenderOperation<T extends GuiRenderOperation<T>> {
    protected double x, y;
    protected Color color;

    public void set(double x, double y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    @SuppressWarnings("unchecked")
    public void run(Pool<T> pool) {
        onRun();
        pool.free((T) this);
    }

    protected abstract void onRun();
}
