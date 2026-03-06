/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.modules.misc;

import florencedevelopment.florenceclient.events.world.PlaySoundEvent;
import florencedevelopment.florenceclient.settings.Setting;
import florencedevelopment.florenceclient.settings.SettingGroup;
import florencedevelopment.florenceclient.settings.SoundEventListSetting;
import florencedevelopment.florenceclient.systems.modules.Categories;
import florencedevelopment.florenceclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;

import java.util.List;

public class SoundBlocker extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<SoundEvent>> sounds = sgGeneral.add(new SoundEventListSetting.Builder()
        .name("sounds")
        .description("Sounds to block.")
        .build()
    );

    public SoundBlocker() {
        super(Categories.Misc, "sound-blocker", "Cancels out selected sounds.");
    }

    @EventHandler
    private void onPlaySound(PlaySoundEvent event) {
        for (SoundEvent sound : sounds.get()) {
            if (sound.id().equals(event.sound.getId())) {
                event.cancel();
                break;
            }
        }
    }

    public boolean shouldBlock(SoundInstance soundInstance) {
        return isActive() && sounds.get().contains(Setting.parseId(Registries.SOUND_EVENT, soundInstance.getId().getPath()));
    }
}
