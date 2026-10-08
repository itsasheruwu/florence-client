/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.animation;

/**
 * Motion design tokens. Durations are in seconds and follow the Material 3 duration buckets.
 */
public final class Motion {
    public static final double FAST = 0.12;
    public static final double BASE = 0.2;
    public static final double SLOW = 0.35;

    private static double speed = 1;
    private static boolean reduced;

    private Motion() {}

    /**
     * Applies the user's motion preferences to every animation.
     *
     * @param speed   1 is the normal speed, 2 twice as fast
     * @param reduced whether animations should be skipped
     */
    public static void configure(double speed, boolean reduced) {
        Motion.speed = Math.max(speed, 0.1);
        Motion.reduced = reduced;
    }

    public static boolean isReduced() {
        return reduced;
    }

    /**
     * Returns the duration an animation of the requested length should take with the current preferences. 0 means the
     * animation should jump straight to its end.
     */
    public static double duration(double seconds) {
        return reduced ? 0 : seconds / speed;
    }
}
