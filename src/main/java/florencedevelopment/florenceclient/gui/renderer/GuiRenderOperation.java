/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.renderer;

import florencedevelopment.florenceclient.utils.misc.Pool;
import florencedevelopment.florenceclient.utils.render.color.Color;

public abstract class GuiRenderOperation<T extends GuiRenderOperation<T>> {
    protected double x, y;
    // Owned by the operation so callers can pass a color they reuse
    protected final Color color = new Color();

    public void set(double x, double y, Color color) {
        this.x = x;
        this.y = y;
        this.color.set(color);
    }

    public void set(double x, double y, int argb) {
        this.x = x;
        this.y = y;
        this.color.set((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, argb >>> 24);
    }

    @SuppressWarnings("unchecked")
    public void run(Pool<T> pool) {
        onRun();
        pool.free((T) this);
    }

    protected abstract void onRun();
}
