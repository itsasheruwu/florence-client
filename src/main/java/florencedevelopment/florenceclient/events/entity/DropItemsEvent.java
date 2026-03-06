/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.entity;

import florencedevelopment.florenceclient.events.Cancellable;
import net.minecraft.item.ItemStack;

public class DropItemsEvent extends Cancellable {
    private static final DropItemsEvent INSTANCE = new DropItemsEvent();

    public ItemStack itemStack;

    public static DropItemsEvent get(ItemStack itemStack) {
        INSTANCE.setCancelled(false);
        INSTANCE.itemStack = itemStack;
        return INSTANCE;
    }
}
