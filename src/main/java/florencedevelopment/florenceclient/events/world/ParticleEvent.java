/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.world;

import florencedevelopment.florenceclient.events.Cancellable;
import net.minecraft.particle.ParticleEffect;

public class ParticleEvent extends Cancellable {
    private static final ParticleEvent INSTANCE = new ParticleEvent();

    public ParticleEffect particle;

    public static ParticleEvent get(ParticleEffect particle) {
        INSTANCE.setCancelled(false);
        INSTANCE.particle = particle;
        return INSTANCE;
    }
}
