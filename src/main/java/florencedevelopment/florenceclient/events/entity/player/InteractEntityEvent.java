/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.entity.player;

import florencedevelopment.florenceclient.events.Cancellable;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;

public class InteractEntityEvent extends Cancellable {
    private static final InteractEntityEvent INSTANCE = new InteractEntityEvent();

    public Entity entity;
    public Hand hand;

    public static InteractEntityEvent get(Entity entity, Hand hand) {
        INSTANCE.setCancelled(false);
        INSTANCE.entity = entity;
        INSTANCE.hand = hand;
        return INSTANCE;
    }
}
