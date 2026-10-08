/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.animation;

/**
 * A damped spring solved in closed form, so the result only depends on the elapsed time and not on how it is split
 * into frames. Based on the solution described in https://www.ryanjuckett.com/damped-springs/.
 */
public final class Spring {
    /** Settles quickly without overshooting. */
    public static final Spring CRITICAL = new Spring(18, 1);
    /** Settles quickly with a small overshoot, good for toggles and pop-ins. */
    public static final Spring SNAPPY = new Spring(16, 0.72);
    /** Slower, with a visible bounce. */
    public static final Spring BOUNCY = new Spring(12, 0.5);

    private final double angularFrequency;
    private final double dampingRatio;

    /**
     * @param angularFrequency speed of the spring in radians per second, higher values are faster
     * @param dampingRatio     below 1 the spring overshoots, at 1 it doesn't, above 1 it is sluggish
     */
    public Spring(double angularFrequency, double dampingRatio) {
        this.angularFrequency = angularFrequency;
        this.dampingRatio = dampingRatio;
    }

    /**
     * Advances the spring.
     *
     * @param position current position
     * @param velocity current velocity
     * @param target   position the spring is pulled towards
     * @param dt       time step in seconds
     * @param out      receives the new position at index 0 and the new velocity at index 1
     */
    public void step(double position, double velocity, double target, double dt, double[] out) {
        if (dt <= 0) {
            out[0] = position;
            out[1] = velocity;
            return;
        }

        double w = angularFrequency;
        double z = dampingRatio;

        double posPos, posVel, velPos, velVel;

        if (z > 1 + 1e-4) {
            // Over-damped
            double za = -w * z;
            double zb = w * Math.sqrt(z * z - 1);
            double z1 = za - zb;
            double z2 = za + zb;

            double e1 = Math.exp(z1 * dt);
            double e2 = Math.exp(z2 * dt);

            double invTwoZb = 1 / (2 * zb);
            double e1OverTwoZb = e1 * invTwoZb;
            double e2OverTwoZb = e2 * invTwoZb;

            double z1e1OverTwoZb = z1 * e1OverTwoZb;
            double z2e2OverTwoZb = z2 * e2OverTwoZb;

            posPos = e1OverTwoZb * z2 - z2e2OverTwoZb + e2;
            posVel = -e1OverTwoZb + e2OverTwoZb;
            velPos = (z1e1OverTwoZb - z2e2OverTwoZb + e2) * z2;
            velVel = -z1e1OverTwoZb + z2e2OverTwoZb;
        }
        else if (z < 1 - 1e-4) {
            // Under-damped
            double omegaZeta = w * z;
            double alpha = w * Math.sqrt(1 - z * z);

            double expTerm = Math.exp(-dt * omegaZeta);
            double cosTerm = Math.cos(dt * alpha);
            double sinTerm = Math.sin(dt * alpha);

            double invAlpha = 1 / alpha;
            double expSin = expTerm * sinTerm;
            double expCos = expTerm * cosTerm;
            double expOmegaZetaSinOverAlpha = expTerm * omegaZeta * sinTerm * invAlpha;

            posPos = expCos + expOmegaZetaSinOverAlpha;
            posVel = expSin * invAlpha;
            velPos = -expSin * alpha - omegaZeta * expOmegaZetaSinOverAlpha;
            velVel = expCos - expOmegaZetaSinOverAlpha;
        }
        else {
            // Critically damped
            double expTerm = Math.exp(-w * dt);
            double timeExp = dt * expTerm;
            double timeExpFreq = timeExp * w;

            posPos = timeExpFreq + expTerm;
            posVel = timeExp;
            velPos = -w * timeExpFreq;
            velVel = -timeExpFreq + expTerm;
        }

        double offset = position - target;

        out[0] = offset * posPos + velocity * posVel + target;
        out[1] = offset * velPos + velocity * velVel;
    }
}
