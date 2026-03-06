/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.renderer;

public enum ShapeMode {
    Lines,
    Sides,
    Both;

    public boolean lines() {
        return this == Lines || this == Both;
    }

    public boolean sides() {
        return this == Sides ||this == Both;
    }
}
