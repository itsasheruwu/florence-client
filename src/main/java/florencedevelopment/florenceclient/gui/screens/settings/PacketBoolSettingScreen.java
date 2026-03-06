/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.screens.settings;

import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.screens.settings.base.CollectionListSettingScreen;
import florencedevelopment.florenceclient.gui.widgets.WWidget;
import florencedevelopment.florenceclient.settings.PacketListSetting;
import florencedevelopment.florenceclient.settings.Setting;
import florencedevelopment.florenceclient.utils.network.PacketUtils;
import net.minecraft.network.packet.Packet;

import java.util.Set;
import java.util.function.Predicate;

public class PacketBoolSettingScreen extends CollectionListSettingScreen<Class<? extends Packet<?>>> {
    public PacketBoolSettingScreen(GuiTheme theme, Setting<Set<Class<? extends Packet<?>>>> setting) {
        super(theme, "Select Packets", setting, setting.get(), PacketUtils.PACKETS);
    }

    @Override
    protected boolean includeValue(Class<? extends Packet<?>> value) {
        Predicate<Class<? extends Packet<?>>> filter = ((PacketListSetting) setting).filter;

        if (filter == null) return true;
        return filter.test(value);
    }

    @Override
    protected WWidget getValueWidget(Class<? extends Packet<?>> value) {
        return theme.label(PacketUtils.getName(value));
    }

    @Override
    protected String[] getValueNames(Class<? extends Packet<?>> value) {
        return new String[]{
            PacketUtils.getName(value),
            value.getSimpleName()
        };
    }
}
