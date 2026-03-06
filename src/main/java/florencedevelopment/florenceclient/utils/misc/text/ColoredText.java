/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.utils.misc.text;

import florencedevelopment.florenceclient.utils.render.color.Color;

/**
 * Encapsulates a string and the color it should have. See {@link TextUtils}
 */
public record ColoredText(String text, Color color) {
}
