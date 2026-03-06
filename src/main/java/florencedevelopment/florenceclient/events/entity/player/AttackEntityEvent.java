/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.entity.player;

import florencedevelopment.florenceclient.events.Cancellable;
import net.minecraft.entity.Entity;

public class AttackEntityEvent extends Cancellable {
    private static final AttackEntityEvent INSTANCE = new AttackEntityEvent();

    public Entity entity;

    public static AttackEntityEvent get(Entity entity) {
        INSTANCE.setCancelled(false);
        INSTANCE.entity = entity;
        return INSTANCE;
    }
}
