/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.widgets;

import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import net.minecraft.item.ItemStack;

public class WItem extends WWidget {
    protected ItemStack itemStack;

    /** Items are drawn by the game on top of everything else, this hides the item while something covers it. */
    public java.util.function.BooleanSupplier hidden;

    public WItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    protected void onCalculateSize() {
        double s = theme.scale(32);

        width = s;
        height = s;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!itemStack.isEmpty() && (hidden == null || !hidden.getAsBoolean())) {
            renderer.post(() -> {
                double s = theme.scale(2);
                renderer.item(itemStack, (int) x, (int) y, (float) s, true);
            });
        }
    }

    public void set(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}
