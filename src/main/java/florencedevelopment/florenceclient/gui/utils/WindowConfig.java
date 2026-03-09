/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.utils;

import florencedevelopment.florenceclient.utils.misc.ISerializable;
import net.minecraft.nbt.NbtCompound;

public class WindowConfig implements ISerializable<WindowConfig> {
    public boolean expanded = true;
    public double x = -1;
    public double y = -1;
    public double width = -1;
    public double height = -1;

    // Saving

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putBoolean("expanded", expanded);
        tag.putDouble("x", x);
        tag.putDouble("y", y);
        tag.putDouble("width", width);
        tag.putDouble("height", height);

        return tag;
    }

    @Override
    public WindowConfig fromTag(NbtCompound tag) {
        tag.getBoolean("expanded").ifPresent(bool -> expanded = bool);
        tag.getDouble("x").ifPresent(x1 -> x = x1);
        tag.getDouble("y").ifPresent(y1 -> y = y1);
        tag.getDouble("width").ifPresent(width1 -> width = width1);
        tag.getDouble("height").ifPresent(height1 -> height = height1);

        return this;
    }
}
