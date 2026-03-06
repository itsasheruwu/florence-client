/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.utils.entity.simulator;

import net.minecraft.util.hit.HitResult;

public class SimulationStep {
    public static final SimulationStep MISS = new SimulationStep(true);

    public boolean shouldStop;
    public HitResult[] hitResults;

    public SimulationStep(boolean stop, HitResult... hitResults) {
        this.shouldStop = stop;
        this.hitResults = hitResults;
    }
}
