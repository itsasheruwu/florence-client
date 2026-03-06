/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.settings;

import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.WidgetScreen;
import florencedevelopment.florenceclient.utils.misc.ICopyable;
import florencedevelopment.florenceclient.utils.misc.ISerializable;

public interface IGeneric<T extends IGeneric<T>> extends ICopyable<T>, ISerializable<T> {
    WidgetScreen createScreen(GuiTheme theme, GenericSetting<T> setting);
}
