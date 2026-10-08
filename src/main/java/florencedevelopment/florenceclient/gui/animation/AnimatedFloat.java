/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.animation;

/**
 * A number that moves towards a target over time, either along an easing curve or pulled by a spring. Call
 * {@link #update(double)} once per frame with the frame time in seconds.
 * <p>
 * Changing the target while moving is always safe. An eased value continues from where it currently is, a spring also
 * keeps its velocity so the motion stays smooth.
 */
public class AnimatedFloat {
    private final double[] stepOut = new double[2];

    private double value;
    private double target;
    private double velocity;

    // Eased movement
    private double from;
    private double duration;
    private double elapsed;
    private Easing easing = Easing.STANDARD;

    // Spring movement, null when eased
    private Spring spring;

    public AnimatedFloat(double value) {
        this.value = value;
        this.from = value;
        this.target = value;
    }

    public double get() {
        return value;
    }

    public double getTarget() {
        return target;
    }

    public boolean isAnimating() {
        return value != target || velocity != 0;
    }

    /**
     * Jumps to the value without animating.
     */
    public void snap(double value) {
        this.value = value;
        this.from = value;
        this.target = value;
        this.velocity = 0;
        this.duration = 0;
        this.elapsed = 0;
    }

    /**
     * Moves to the target along an easing curve. The duration is adjusted by the user's {@link Motion} preferences.
     */
    public void animateTo(double target, double seconds, Easing easing) {
        if (target == this.target && spring == null) return;

        double scaled = Motion.duration(seconds);

        if (scaled <= 0) {
            snap(target);
            return;
        }

        this.spring = null;
        this.from = value;
        this.target = target;
        this.duration = scaled;
        this.elapsed = 0;
        this.easing = easing;
        this.velocity = 0;
    }

    public void animateTo(double target, double seconds) {
        animateTo(target, seconds, Easing.STANDARD);
    }

    /**
     * Moves to the target pulled by a spring.
     */
    public void springTo(double target, Spring spring) {
        if (Motion.isReduced()) {
            snap(target);
            return;
        }

        this.spring = spring;
        this.target = target;
    }

    /**
     * Advances the animation.
     *
     * @return the new value
     */
    public double update(double dt) {
        if (dt <= 0) return value;

        if (spring != null) {
            if (value == target && velocity == 0) return value;

            spring.step(value, velocity, target, dt, stepOut);
            value = stepOut[0];
            velocity = stepOut[1];

            // Stop once the movement is too small to see
            if (Math.abs(value - target) < 1e-4 && Math.abs(velocity) < 1e-3) {
                value = target;
                velocity = 0;
            }
        }
        else if (value != target) {
            elapsed += dt;

            if (elapsed >= duration) {
                value = target;
            }
            else {
                double progress = easing.apply(elapsed / duration);
                value = from + (target - from) * progress;
            }
        }

        return value;
    }
}
