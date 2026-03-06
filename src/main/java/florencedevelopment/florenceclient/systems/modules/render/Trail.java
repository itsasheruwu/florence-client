/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.modules.render;

import florencedevelopment.florenceclient.events.world.TickEvent;
import florencedevelopment.florenceclient.settings.BoolSetting;
import florencedevelopment.florenceclient.settings.ParticleTypeListSetting;
import florencedevelopment.florenceclient.settings.Setting;
import florencedevelopment.florenceclient.settings.SettingGroup;
import florencedevelopment.florenceclient.systems.modules.Categories;
import florencedevelopment.florenceclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;

import java.util.List;

public class Trail extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<ParticleType<?>>> particles = sgGeneral.add(new ParticleTypeListSetting.Builder()
        .name("particles")
        .description("Particles to draw.")
        .defaultValue(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, ParticleTypes.CAMPFIRE_COSY_SMOKE)
        .build()
    );

    private final Setting<Boolean> pause = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-when-stationary")
        .description("Whether or not to add particles when you are not moving.")
        .defaultValue(true)
        .build()
    );

    public Trail() {
        super(Categories.Render, "trail", "Renders a customizable trail behind your player.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (pause.get()
            && mc.player.getX() == mc.player.lastX
            && mc.player.getY() == mc.player.lastY
            && mc.player.getZ() == mc.player.lastZ) return;

        for (ParticleType<?> particleType : particles.get()) {
            mc.world.addParticleClient((ParticleEffect) particleType, mc.player.getX(), mc.player.getY(), mc.player.getZ(), 0, 0, 0);
        }
    }
}
