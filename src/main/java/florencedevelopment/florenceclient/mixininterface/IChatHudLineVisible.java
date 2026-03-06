/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixininterface;

public interface IChatHudLineVisible extends IChatHudLine {
    boolean florence$isStartOfEntry();
    void florence$setStartOfEntry(boolean start);
}
