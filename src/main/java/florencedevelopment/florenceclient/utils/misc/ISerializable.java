/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.utils.misc;

import net.minecraft.nbt.NbtCompound;

public interface ISerializable<T> {
    NbtCompound toTag();

    T fromTag(NbtCompound tag);
}
