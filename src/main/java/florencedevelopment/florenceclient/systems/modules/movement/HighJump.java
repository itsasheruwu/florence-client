/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.modules.movement;

import florencedevelopment.florenceclient.events.entity.player.JumpVelocityMultiplierEvent;
import florencedevelopment.florenceclient.settings.DoubleSetting;
import florencedevelopment.florenceclient.settings.Setting;
import florencedevelopment.florenceclient.settings.SettingGroup;
import florencedevelopment.florenceclient.systems.modules.Categories;
import florencedevelopment.florenceclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class HighJump extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> multiplier = sgGeneral.add(new DoubleSetting.Builder()
        .name("jump-multiplier")
        .description("Jump height multiplier.")
        .defaultValue(1)
        .min(0)
        .build()
    );

    public HighJump() {
        super(Categories.Movement, "high-jump", "Makes you jump higher than normal.");
    }

    @EventHandler
    private void onJumpVelocityMultiplier(JumpVelocityMultiplierEvent event) {
        event.multiplier *= multiplier.get();
    }
}
