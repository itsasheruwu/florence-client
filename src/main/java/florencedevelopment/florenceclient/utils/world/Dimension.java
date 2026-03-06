/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.utils.world;

public enum Dimension {
    Overworld,
    Nether,
    End;

    public Dimension opposite() {
        return switch (this) {
            case Overworld -> Nether;
            case Nether -> Overworld;
            default -> this;
        };
    }
}
