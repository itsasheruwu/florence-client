/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.animation;

/**
 * The hover and press animation almost every control has. Call {@link #update} once per frame.
 */
public class Interaction {
    private final AnimatedFloat hover = new AnimatedFloat(0);
    private final AnimatedFloat press = new AnimatedFloat(0);

    public void update(boolean hovered, boolean pressed, double dt) {
        hover.animateTo(hovered ? 1 : 0, Motion.FAST, Easing.STANDARD);
        press.animateTo(pressed ? 1 : 0, 0.08, Easing.STANDARD);

        hover.update(dt);
        press.update(dt);
    }

    /**
     * 0 when the mouse is away, 1 when it is over.
     */
    public double hover() {
        return hover.get();
    }

    /**
     * 0 when released, 1 when pressed.
     */
    public double press() {
        return press.get();
    }

    public boolean isAnimating() {
        return hover.isAnimating() || press.isAnimating();
    }
}
