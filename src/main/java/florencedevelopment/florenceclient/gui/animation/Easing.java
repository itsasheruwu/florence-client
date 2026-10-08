/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.animation;

/**
 * Maps animation progress (0 to 1) to eased progress. Every curve returns 0 at 0 and 1 at 1.
 */
public enum Easing {
    LINEAR {
        @Override
        public double apply(double t) {
            return t;
        }
    },

    EASE_OUT_CUBIC {
        @Override
        public double apply(double t) {
            double u = 1 - t;
            return 1 - u * u * u;
        }
    },

    EASE_IN_CUBIC {
        @Override
        public double apply(double t) {
            return t * t * t;
        }
    },

    EASE_IN_OUT_CUBIC {
        @Override
        public double apply(double t) {
            if (t < 0.5) return 4 * t * t * t;

            double u = -2 * t + 2;
            return 1 - u * u * u / 2;
        }
    },

    EASE_OUT_QUINT {
        @Override
        public double apply(double t) {
            double u = 1 - t;
            return 1 - u * u * u * u * u;
        }
    },

    EASE_OUT_EXPO {
        @Override
        public double apply(double t) {
            return t >= 1 ? 1 : 1 - Math.pow(2, -10 * t);
        }
    },

    /** Overshoots the target slightly before settling. */
    EASE_OUT_BACK {
        @Override
        public double apply(double t) {
            double c1 = 1.70158;
            double c3 = c1 + 1;
            double u = t - 1;
            return 1 + c3 * u * u * u + c1 * u * u;
        }
    },

    /** Material 3 "standard" curve. */
    STANDARD {
        private final CubicBezier bezier = new CubicBezier(0.2, 0, 0, 1);

        @Override
        public double apply(double t) {
            return bezier.apply(t);
        }
    },

    /** Material 3 "emphasized decelerate" curve, for elements entering the screen. */
    EMPHASIZED_DECELERATE {
        private final CubicBezier bezier = new CubicBezier(0.05, 0.7, 0.1, 1);

        @Override
        public double apply(double t) {
            return bezier.apply(t);
        }
    },

    /** Material 3 "emphasized accelerate" curve, for elements leaving the screen. */
    EMPHASIZED_ACCELERATE {
        private final CubicBezier bezier = new CubicBezier(0.3, 0, 0.8, 0.15);

        @Override
        public double apply(double t) {
            return bezier.apply(t);
        }
    };

    public abstract double apply(double t);

    /**
     * Cubic bezier easing like the CSS {@code cubic-bezier()} function, with the end points fixed at (0, 0) and (1, 1).
     */
    static final class CubicBezier {
        private final double x1, y1, x2, y2;

        CubicBezier(double x1, double y1, double x2, double y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        double apply(double x) {
            if (x <= 0) return 0;
            if (x >= 1) return 1;

            // Find the curve parameter whose x matches, using bisection as the x curve is monotonic for x1, x2 in [0, 1]
            double low = 0;
            double high = 1;
            double t = x;

            for (int i = 0; i < 24; i++) {
                double currentX = sample(t, x1, x2);

                if (Math.abs(currentX - x) < 1e-7) break;

                if (currentX < x) low = t;
                else high = t;

                t = (low + high) / 2;
            }

            return sample(t, y1, y2);
        }

        private static double sample(double t, double p1, double p2) {
            double u = 1 - t;
            return 3 * u * u * t * p1 + 3 * u * t * t * p2 + t * t * t;
        }
    }
}
