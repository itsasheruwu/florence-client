/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.modules.combat;

import florencedevelopment.florenceclient.events.world.TickEvent;
import florencedevelopment.florenceclient.settings.BoolSetting;
import florencedevelopment.florenceclient.settings.DoubleSetting;
import florencedevelopment.florenceclient.settings.EnumSetting;
import florencedevelopment.florenceclient.settings.Setting;
import florencedevelopment.florenceclient.settings.SettingGroup;
import florencedevelopment.florenceclient.systems.modules.Categories;
import florencedevelopment.florenceclient.systems.modules.Module;
import florencedevelopment.florenceclient.utils.entity.SortPriority;
import florencedevelopment.florenceclient.utils.entity.Target;
import florencedevelopment.florenceclient.utils.entity.TargetUtils;
import florencedevelopment.florenceclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.MaceItem;
import net.minecraft.util.Hand;

public class MaceAssist extends Module {
    private static final double NEAR_READY_BUFFER = 0.75;
    private static final int HIT_LOCKOUT_TICKS = 2;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("target-range")
        .description("How far away targets can be.")
        .defaultValue(5.5)
        .min(1)
        .sliderRange(1, 8)
        .build()
    );

    private final Setting<SortPriority> priority = sgGeneral.add(new EnumSetting.Builder<SortPriority>()
        .name("target-priority")
        .description("How targets are sorted.")
        .defaultValue(SortPriority.ClosestAngle)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates toward the tracked target during mace smash windows.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Target> aimTarget = sgGeneral.add(new EnumSetting.Builder<Target>()
        .name("aim-target")
        .description("Which part of the target to aim at.")
        .defaultValue(Target.Body)
        .build()
    );

    private final Setting<Double> minimumFallDistance = sgGeneral.add(new DoubleSetting.Builder()
        .name("minimum-fall-distance")
        .description("Minimum fall distance before the smash is considered ready.")
        .defaultValue(1.5)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );

    private final Setting<Boolean> onlyWhileAttackHeld = sgGeneral.add(new BoolSetting.Builder()
        .name("only-while-attack-held")
        .description("Only assists while the attack key is held.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> autoHit = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-hit")
        .description("Automatically swings when a valid smash hit is ready.")
        .defaultValue(true)
        .build()
    );

    private PlayerEntity target;
    private State state = State.Idle;
    private int hitLockout;

    public MaceAssist() {
        super(Categories.Combat, "mace-assist", "Tracks targets, lines up mace smash hits, and can auto-swing when the hit is ready.");
    }

    @Override
    public void onDeactivate() {
        target = null;
        state = State.Idle;
        hitLockout = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (hitLockout > 0) hitLockout--;
        state = State.Idle;

        if (!isHoldingMace()) return;
        if (onlyWhileAttackHeld.get() && !mc.options.attackKey.isPressed()) return;
        if (mc.player == null || mc.world == null) return;
        if (mc.player.isGliding() || mc.player.isClimbing() || mc.player.isSubmergedInWater() || mc.player.isInLava()) return;

        target = TargetUtils.getPlayerTarget(range.get(), priority.get());
        if (TargetUtils.isBadTarget(target, range.get())) {
            target = null;
            return;
        }

        if (isSmashReady()) {
            state = State.Ready;
        } else if (isNearSmashReady()) {
            state = State.Tracking;
        } else {
            state = State.Locked;
        }

        if (rotate.get() && (state == State.Ready || state == State.Tracking)) {
            Rotations.rotate(Rotations.getYaw(target), Rotations.getPitch(target, aimTarget.get()), 25);
        }

        if (state == State.Ready && autoHit.get()) {
            tryAttack();
        }
    }

    @Override
    public String getInfoString() {
        return switch (state) {
            case Idle -> null;
            case Locked -> "Locked";
            case Tracking -> "Tracking";
            case Ready -> "Ready";
        };
    }

    private boolean isHoldingMace() {
        return mc.player != null && mc.player.getMainHandStack().getItem() instanceof MaceItem;
    }

    private boolean isSmashReady() {
        return !mc.player.isOnGround() && mc.player.fallDistance >= minimumFallDistance.get();
    }

    private boolean isNearSmashReady() {
        return !mc.player.isOnGround()
            && mc.player.getVelocity().y < 0
            && mc.player.fallDistance >= Math.max(0, minimumFallDistance.get() - NEAR_READY_BUFFER);
    }

    private void tryAttack() {
        if (!autoHit.get()) return;
        if (target == null || mc.player == null || mc.interactionManager == null) return;
        if (hitLockout > 0) return;
        if (TargetUtils.isBadTarget(target, range.get())) return;
        if (!isHoldingMace()) return;
        if (mc.player.getAttackCooldownProgress(0.5f) < 1.0f) return;
        if (mc.player.isUsingItem()) return;

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        hitLockout = HIT_LOCKOUT_TICKS;
    }

    private enum State {
        Idle,
        Locked,
        Tracking,
        Ready
    }
}
