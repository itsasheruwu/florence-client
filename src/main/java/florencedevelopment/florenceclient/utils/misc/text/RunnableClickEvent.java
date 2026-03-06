/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.utils.misc.text;

/**
 * Allows arbitrary code execution in a click event
 */
public class RunnableClickEvent extends FlorenceClickEvent {
    public final Runnable runnable;

    public RunnableClickEvent(Runnable runnable) {
        super(null); // Should ensure no vanilla code is triggered, and only we handle it
        this.runnable = runnable;
    }
}
