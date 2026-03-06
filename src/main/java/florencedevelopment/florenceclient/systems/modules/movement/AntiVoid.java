/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.modules.movement;

import florencedevelopment.florenceclient.events.world.TickEvent;
import florencedevelopment.florenceclient.settings.EnumSetting;
import florencedevelopment.florenceclient.settings.Setting;
import florencedevelopment.florenceclient.settings.SettingGroup;
import florencedevelopment.florenceclient.systems.modules.Categories;
import florencedevelopment.florenceclient.systems.modules.Module;
import florencedevelopment.florenceclient.systems.modules.Modules;
import florencedevelopment.florenceclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;

public class AntiVoid extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("The method to prevent you from falling into the void.")
        .defaultValue(Mode.Jump)
        .onChanged(a -> onActivate())
        .build()
    );

    private boolean wasFlightEnabled, hasRun;

    public AntiVoid() {
        super(Categories.Movement, "anti-void", "Attempts to prevent you from falling into the void.");
    }

    @Override
    public void onActivate() {
        if (mode.get() == Mode.Flight) wasFlightEnabled = Modules.get().isActive(Flight.class);
    }

    @Override
    public void onDeactivate() {
        if (!wasFlightEnabled && mode.get() == Mode.Flight && Utils.canUpdate()) {
            Modules.get().get(Flight.class).disable();
        }
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        int minY = mc.world.getBottomY();

        if (mc.player.getY() > minY || mc.player.getY() < minY - 15) {
            if (hasRun && mode.get() == Mode.Flight) {
                Modules.get().get(Flight.class).disable();
                hasRun = false;
            }
            return;
        }

        switch (mode.get()) {
            case Flight -> {
                Modules.get().get(Flight.class).enable();
                hasRun = true;
            }
            case Jump -> mc.player.jump();
        }
    }

    public enum Mode {
        Flight,
        Jump
    }
}
