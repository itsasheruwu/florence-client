/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.modules.movement.speed;

import florencedevelopment.florenceclient.events.entity.player.PlayerMoveEvent;
import florencedevelopment.florenceclient.events.packets.PacketEvent;
import florencedevelopment.florenceclient.events.world.TickEvent;
import florencedevelopment.florenceclient.settings.*;
import florencedevelopment.florenceclient.systems.modules.Categories;
import florencedevelopment.florenceclient.systems.modules.Module;
import florencedevelopment.florenceclient.systems.modules.Modules;
import florencedevelopment.florenceclient.systems.modules.movement.speed.modes.Strafe;
import florencedevelopment.florenceclient.systems.modules.movement.speed.modes.Vanilla;
import florencedevelopment.florenceclient.systems.modules.world.Timer;
import florencedevelopment.florenceclient.utils.entity.EntityUtils;
import florencedevelopment.florenceclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.MovementType;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class Speed extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<SpeedModes> speedMode = sgGeneral.add(new EnumSetting.Builder<SpeedModes>()
        .name("mode")
        .description("The method of applying speed.")
        .defaultValue(SpeedModes.Vanilla)
        .onModuleActivated(speedModesSetting -> onSpeedModeChanged(speedModesSetting.get()))
        .onChanged(this::onSpeedModeChanged)
        .build()
    );

    public final Setting<Double> vanillaSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("vanilla-speed")
        .description("The speed in blocks per second.")
        .defaultValue(5.6)
        .min(0)
        .sliderMax(20)
        .visible(() -> speedMode.get() == SpeedModes.Vanilla)
        .build()
    );

    public final Setting<Double> ncpSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("strafe-speed")
        .description("The speed.")
        .visible(() -> speedMode.get() == SpeedModes.Strafe)
        .defaultValue(1.6)
        .min(0)
        .sliderMax(3)
        .build()
    );

    public final Setting<Boolean> ncpSpeedLimit = sgGeneral.add(new BoolSetting.Builder()
        .name("speed-limit")
        .description("Limits your speed on servers with very strict anticheats.")
        .visible(() -> speedMode.get() == SpeedModes.Strafe)
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> strafeDamageBoost = sgGeneral.add(new BoolSetting.Builder()
        .name("damage-boost")
        .description("Boosts strafe speed while you are taking knockback damage.")
        .visible(() -> speedMode.get() == SpeedModes.Strafe)
        .defaultValue(false)
        .build()
    );

    public final Setting<Double> strafeDamageBoostMultiplier = sgGeneral.add(new DoubleSetting.Builder()
        .name("damage-boost-multiplier")
        .description("How much to multiply strafe speed by after taking damage.")
        .visible(() -> speedMode.get() == SpeedModes.Strafe && strafeDamageBoost.get())
        .defaultValue(1.2)
        .min(0)
        .sliderMin(0)
        .sliderMax(3)
        .build()
    );

    public final Setting<Double> timer = sgGeneral.add(new DoubleSetting.Builder()
        .name("timer")
        .description("Timer override.")
        .defaultValue(1)
        .min(0.01)
        .sliderMin(0.01)
        .sliderMax(10)
        .build()
    );

    public final Setting<Boolean> inLiquids = sgGeneral.add(new BoolSetting.Builder()
        .name("in-liquids")
        .description("Uses speed when in lava or water.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> whenSneaking = sgGeneral.add(new BoolSetting.Builder()
        .name("when-sneaking")
        .description("Uses speed when sneaking.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> vanillaOnGround = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-ground")
        .description("Uses speed only when standing on a block.")
        .visible(() -> speedMode.get() == SpeedModes.Vanilla)
        .defaultValue(false)
        .build()
    );

    private SpeedMode currentMode;

    public Speed() {
        super(Categories.Movement, "speed", "Modifies your movement speed when moving on the ground.");

        onSpeedModeChanged(speedMode.get());
    }

    @Override
    public void onActivate() {
        currentMode.onActivate();
    }

    @Override
    public void onDeactivate() {
        Modules.get().get(Timer.class).setOverride(Timer.OFF);
        currentMode.onDeactivate();
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (event.type != MovementType.SELF) return;
        if (stopSpeed()) {
            Modules.get().get(Timer.class).setOverride(Timer.OFF);
            return;
        }

        if (timer.get() != Timer.OFF) {
            Modules.get().get(Timer.class).setOverride(PlayerUtils.isMoving() ? timer.get() : Timer.OFF);
        }

        currentMode.onMove(event);
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (stopSpeed()) return;

        currentMode.onTick();
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket) currentMode.onRubberband();
    }

    private void onSpeedModeChanged(SpeedModes mode) {
        switch (mode) {
            case Vanilla -> currentMode = new Vanilla();
            case Strafe -> currentMode = new Strafe();
        }
    }

    private boolean stopSpeed() {
        if (mc.player.isGliding() || mc.player.isClimbing() || mc.player.getVehicle() != null) return true;
        if (speedMode.get() == SpeedModes.Strafe && EntityUtils.isInCobweb(mc.player)) return true;
        if (!whenSneaking.get() && mc.player.isSneaking()) return true;
        if (vanillaOnGround.get() && !mc.player.isOnGround() && speedMode.get() == SpeedModes.Vanilla) return true;
        return !inLiquids.get() && (mc.player.isTouchingWater() || mc.player.isInLava());
    }

    @Override
    public String getInfoString() {
        return currentMode.getHudString();
    }
}
