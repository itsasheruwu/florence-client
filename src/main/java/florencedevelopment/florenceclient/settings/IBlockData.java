/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.settings;

import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.WidgetScreen;
import florencedevelopment.florenceclient.utils.misc.IChangeable;
import florencedevelopment.florenceclient.utils.misc.ICopyable;
import florencedevelopment.florenceclient.utils.misc.ISerializable;
import net.minecraft.block.Block;

public interface IBlockData<T extends ICopyable<T> & ISerializable<T> & IChangeable & IBlockData<T>> {
    WidgetScreen createScreen(GuiTheme theme, Block block, BlockDataSetting<T> setting);
}
