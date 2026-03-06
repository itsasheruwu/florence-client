/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.entity.player;

/**
 * @see net.minecraft.client.network.ClientPlayerEntity#tickMovement()
 */
public class PlayerTickMovementEvent {
    private static final PlayerTickMovementEvent INSTANCE = new PlayerTickMovementEvent();

    public static PlayerTickMovementEvent get() {
        return INSTANCE;
    }
}
