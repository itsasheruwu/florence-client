/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.modules.movement;

import florencedevelopment.florenceclient.events.packets.PacketEvent;
import florencedevelopment.florenceclient.events.world.TickEvent;
import florencedevelopment.florenceclient.mixininterface.IPlayerInteractEntityC2SPacket;
import florencedevelopment.florenceclient.settings.BoolSetting;
import florencedevelopment.florenceclient.settings.EnumSetting;
import florencedevelopment.florenceclient.settings.Setting;
import florencedevelopment.florenceclient.settings.SettingGroup;
import florencedevelopment.florenceclient.systems.modules.Categories;
import florencedevelopment.florenceclient.systems.modules.Module;
import florencedevelopment.florenceclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

public class Sprint extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public enum Mode {
        Strict,
        Rage
    }

    public final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("sprint-mode")
        .description("What mode of sprinting.")
        .defaultValue(Mode.Strict)
        .build()
    );

    private final Setting<Boolean> keepSprint = sgGeneral.add(new BoolSetting.Builder()
        .name("keep-sprint")
        .description("Whether to keep sprinting after attacking.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> unsprintOnHit = sgGeneral.add(new BoolSetting.Builder()
        .name("unsprint-on-hit")
        .description("Whether to stop sprinting before attacking, to ensure you get crits and sweep attacks.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> unsprintInWater = sgGeneral.add(new BoolSetting.Builder()
        .name("unsprint-in-water")
        .description("Whether to stop sprinting when in water.")
        .defaultValue(true)
        .visible(() -> mode.get() == Mode.Rage)
        .build()
    );

    private final Setting<Boolean> permaSprint = sgGeneral.add(new BoolSetting.Builder()
        .name("sprint-while-stationary")
        .description("Sprint even when not moving.")
        .defaultValue(false)
        .visible(() -> mode.get() == Mode.Rage)
        .build()
    );

    public Sprint() {
        super(Categories.Movement, "sprint", "Automatically sprints.");
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onTickMovement(TickEvent.Post event) {
        if (unsprintInWater.get() && mc.player.isTouchingWater()) return;

        mc.player.setSprinting(shouldSprint());
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onPacketSend(PacketEvent.Send event) {
        if (!unsprintOnHit.get()) return;
        if (!(event.packet instanceof IPlayerInteractEntityC2SPacket packet)
            || packet.florence$getType() != PlayerInteractEntityC2SPacket.InteractType.ATTACK) return;

        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
        mc.player.setSprinting(false);
    }

    @EventHandler
    private void onPacketSent(PacketEvent.Sent event) {
        if (!unsprintOnHit.get() || !keepSprint.get()) return;
        if (!(event.packet instanceof IPlayerInteractEntityC2SPacket packet)
            || packet.florence$getType() != PlayerInteractEntityC2SPacket.InteractType.ATTACK) return;

        if (!shouldSprint() || mc.player.isSprinting()) return;

        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        mc.player.setSprinting(true);
    }

    public boolean shouldSprint() {
        if (mc.currentScreen != null && !Modules.get().get(GUIMove.class).sprint.get()) return false;

        float movement = mode.get() == Mode.Rage
            ? (Math.abs(mc.player.forwardSpeed) + Math.abs(mc.player.sidewaysSpeed))
            : mc.player.forwardSpeed;

        if (movement <= (mc.player.isSubmergedInWater() ? 1.0E-5F : 0.8)) {
            if (mode.get() == Mode.Strict || !permaSprint.get()) return false;
        }

        boolean strictSprint = !(mc.player.isPartlyTouchingWater())
            && !mc.player.hasBlindnessEffect()
            && mc.player.hasVehicle() ? (mc.player.getVehicle().canSprintAsVehicle() && mc.player.getVehicle().isLogicalSideForUpdatingMovement()) : mc.player.getHungerManager().canSprint()
            && (!mc.player.horizontalCollision || mc.player.collidedSoftly);

        return isActive() && (mode.get() == Mode.Rage || strictSprint);
    }

    public boolean rageSprint() {
        return isActive() && mode.get() == Mode.Rage;
    }

    public boolean unsprintInWater() {
        return isActive() && unsprintInWater.get();
    }

    public boolean stopSprinting() {
        return !isActive() || !keepSprint.get();
    }
}
