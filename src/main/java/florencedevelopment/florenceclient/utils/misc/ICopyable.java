/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.utils.misc;

public interface ICopyable<T extends ICopyable<T>> {
    T set(T value);

    T copy();
}
