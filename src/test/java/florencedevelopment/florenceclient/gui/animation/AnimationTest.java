/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.animation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnimationTest {
    @AfterEach
    void resetMotion() {
        Motion.configure(1, false);
    }

    @Test
    void everyEasingStartsAtZeroAndEndsAtOne() {
        for (Easing easing : Easing.values()) {
            assertEquals(0, easing.apply(0), 1e-9, easing + " at 0");
            assertEquals(1, easing.apply(1), 1e-9, easing + " at 1");
        }
    }

    @Test
    void bezierEasingsNeverMoveBackwards() {
        Easing[] curves = {Easing.STANDARD, Easing.EMPHASIZED_DECELERATE, Easing.EMPHASIZED_ACCELERATE};

        for (Easing easing : curves) {
            double previous = 0;

            for (int i = 1; i <= 200; i++) {
                double value = easing.apply(i / 200.0);

                assertTrue(value >= previous - 1e-9, easing + " went backwards at step " + i);
                previous = value;
            }
        }
    }

    @Test
    void decelerateIsFastAtTheStartAndAccelerateIsSlow() {
        assertTrue(Easing.EMPHASIZED_DECELERATE.apply(0.25) > 0.5);
        assertTrue(Easing.EMPHASIZED_ACCELERATE.apply(0.25) < 0.25);
    }

    @Test
    void easeOutBackOvershoots() {
        double max = 0;

        for (int i = 0; i <= 100; i++) max = Math.max(max, Easing.EASE_OUT_BACK.apply(i / 100.0));

        assertTrue(max > 1);
    }

    @Test
    void springSettlesOnTheTarget() {
        Spring[] springs = {Spring.CRITICAL, Spring.SNAPPY, Spring.BOUNCY, new Spring(10, 2.5)};

        for (Spring spring : springs) {
            double[] state = {0, 0};
            double[] out = new double[2];

            for (int i = 0; i < 600; i++) {
                spring.step(state[0], state[1], 10, 1 / 60.0, out);
                state[0] = out[0];
                state[1] = out[1];
            }

            assertEquals(10, state[0], 1e-3);
            assertEquals(0, state[1], 1e-3);
        }
    }

    @Test
    void criticalSpringNeverOvershoots() {
        double[] state = {0, 0};
        double[] out = new double[2];

        for (int i = 0; i < 300; i++) {
            Spring.CRITICAL.step(state[0], state[1], 1, 1 / 120.0, out);
            state[0] = out[0];
            state[1] = out[1];

            assertTrue(state[0] <= 1 + 1e-9);
        }
    }

    @Test
    void underDampedSpringOvershoots() {
        double[] state = {0, 0};
        double[] out = new double[2];
        double max = 0;

        for (int i = 0; i < 300; i++) {
            Spring.BOUNCY.step(state[0], state[1], 1, 1 / 120.0, out);
            state[0] = out[0];
            state[1] = out[1];
            max = Math.max(max, state[0]);
        }

        assertTrue(max > 1.01);
    }

    @Test
    void springResultDoesNotDependOnFrameRate() {
        Spring[] springs = {Spring.CRITICAL, Spring.SNAPPY, new Spring(10, 2.5)};

        for (Spring spring : springs) {
            double[] slow = run(spring, 30);
            double[] fast = run(spring, 240);

            assertEquals(fast[0], slow[0], 1e-9);
            assertEquals(fast[1], slow[1], 1e-9);
        }
    }

    private static double[] run(Spring spring, int fps) {
        double[] state = {0, 0};
        double[] out = new double[2];

        // Half a second at the requested frame rate
        for (int i = 0; i < fps / 2; i++) {
            spring.step(state[0], state[1], 5, 1.0 / fps, out);
            state[0] = out[0];
            state[1] = out[1];
        }

        return state;
    }

    @Test
    void stepWithoutElapsedTimeChangesNothing() {
        double[] out = new double[2];
        Spring.SNAPPY.step(3, 2, 10, 0, out);

        assertEquals(3, out[0]);
        assertEquals(2, out[1]);
    }

    @Test
    void animatedFloatReachesTheTargetAfterItsDuration() {
        AnimatedFloat value = new AnimatedFloat(0);
        value.animateTo(10, 0.2, Easing.LINEAR);

        value.update(0.1);
        assertEquals(5, value.get(), 1e-9);
        assertTrue(value.isAnimating());

        value.update(0.1);
        assertEquals(10, value.get(), 1e-9);
        assertFalse(value.isAnimating());
    }

    @Test
    void animatedFloatRetargetsFromWhereItIs() {
        AnimatedFloat value = new AnimatedFloat(0);
        value.animateTo(10, 1, Easing.LINEAR);
        value.update(0.5);

        value.animateTo(0, 1, Easing.LINEAR);
        assertEquals(5, value.get(), 1e-9);

        value.update(0.5);
        assertEquals(2.5, value.get(), 1e-9);
    }

    @Test
    void animatedFloatSpringKeepsItsVelocityWhenRetargeted() {
        AnimatedFloat value = new AnimatedFloat(0);
        value.springTo(1, Spring.SNAPPY);

        for (int i = 0; i < 5; i++) value.update(1 / 60.0);
        double before = value.get();

        value.springTo(0, Spring.SNAPPY);
        value.update(1 / 60.0);

        // It was still moving towards 1, so it carries on a little before turning around
        assertTrue(value.get() > before);
    }

    @Test
    void reducedMotionSnapsStraightToTheTarget() {
        Motion.configure(1, true);

        AnimatedFloat tween = new AnimatedFloat(0);
        tween.animateTo(7, 0.3);
        assertEquals(7, tween.get());

        AnimatedFloat spring = new AnimatedFloat(0);
        spring.springTo(3, Spring.SNAPPY);
        assertEquals(3, spring.get());
    }

    @Test
    void animationSpeedScalesDurations() {
        Motion.configure(2, false);

        assertEquals(0.1, Motion.duration(0.2), 1e-9);

        AnimatedFloat value = new AnimatedFloat(0);
        value.animateTo(1, 0.2, Easing.LINEAR);
        value.update(0.1);

        assertEquals(1, value.get(), 1e-9);
    }

    @Test
    void snapStopsAnyMovement() {
        AnimatedFloat value = new AnimatedFloat(0);
        value.springTo(10, Spring.BOUNCY);
        value.update(0.05);

        value.snap(4);
        value.update(0.5);

        assertEquals(4, value.get());
        assertFalse(value.isAnimating());
    }
}
