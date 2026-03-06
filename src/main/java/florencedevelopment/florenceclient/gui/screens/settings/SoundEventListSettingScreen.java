/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.screens.settings;

import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.screens.settings.base.CollectionListSettingScreen;
import florencedevelopment.florenceclient.gui.widgets.WWidget;
import florencedevelopment.florenceclient.settings.Setting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;

import java.util.List;

public class SoundEventListSettingScreen extends CollectionListSettingScreen<SoundEvent> {
    public SoundEventListSettingScreen(GuiTheme theme, Setting<List<SoundEvent>> setting) {
        super(theme, "Select Sounds", setting, setting.get(), Registries.SOUND_EVENT);
    }

    @Override
    protected WWidget getValueWidget(SoundEvent value) {
        return theme.label(value.id().getPath());
    }

    @Override
    protected String[] getValueNames(SoundEvent value) {
        return new String[]{
            value.id().toString(),
            I18n.translate("subtitles." + value.id().getPath())
        };
    }
}
