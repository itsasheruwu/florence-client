/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.modules.movement;

import florencedevelopment.florenceclient.events.render.Render3DEvent;
import florencedevelopment.florenceclient.settings.BoolSetting;
import florencedevelopment.florenceclient.settings.ColorSetting;
import florencedevelopment.florenceclient.settings.DoubleSetting;
import florencedevelopment.florenceclient.settings.EnumSetting;
import florencedevelopment.florenceclient.settings.IntSetting;
import florencedevelopment.florenceclient.settings.Setting;
import florencedevelopment.florenceclient.settings.SettingGroup;
import florencedevelopment.florenceclient.systems.modules.Categories;
import florencedevelopment.florenceclient.systems.modules.Module;
import florencedevelopment.florenceclient.systems.modules.Modules;
import florencedevelopment.florenceclient.systems.modules.combat.KillAura;
import florencedevelopment.florenceclient.systems.modules.movement.speed.Speed;
import florencedevelopment.florenceclient.systems.modules.movement.speed.SpeedModes;
import florencedevelopment.florenceclient.utils.Utils;
import florencedevelopment.florenceclient.utils.player.PlayerUtils;
import florencedevelopment.florenceclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2d;

public class TargetStrafe extends Module {
    private static final double RADIUS_TOLERANCE = 0.15;
    private static final double RADIAL_WEIGHT = 0.5;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Double> radius = sgGeneral.add(new DoubleSetting.Builder()
        .name("radius")
        .description("How far away to orbit from the target.")
        .defaultValue(2.0)
        .range(0.5, 6.0)
        .sliderRange(0.5, 6.0)
        .build()
    );

    private final Setting<Direction> direction = sgGeneral.add(new EnumSetting.Builder<Direction>()
        .name("direction")
        .description("The initial orbit direction.")
        .defaultValue(Direction.Left)
        .build()
    );

    private final Setting<Boolean> autoSwitch = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-switch")
        .description("Automatically flips direction on collision or unsafe orbit steps.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> onlyOnJump = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-jump")
        .description("Only strafes while holding jump.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> requireInput = sgGeneral.add(new BoolSetting.Builder()
        .name("require-input")
        .description("Only strafes while you are pressing movement keys.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> checkVoid = sgGeneral.add(new BoolSetting.Builder()
        .name("check-void")
        .description("Avoids orbiting into unsafe ground.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("Renders the target orbit ring.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The color of the orbit ring.")
        .defaultValue(new SettingColor(197, 137, 232))
        .visible(render::get)
        .build()
    );

    private final Setting<Integer> segments = sgRender.add(new IntSetting.Builder()
        .name("segments")
        .description("How smooth the orbit ring is.")
        .defaultValue(48)
        .range(16, 96)
        .sliderRange(16, 96)
        .visible(render::get)
        .build()
    );

    private int currentDirection;
    private int lastTargetId = -1;
    private boolean wasCollidingHorizontally;

    public TargetStrafe() {
        super(Categories.Movement, "target-strafe", "Circles around KillAura targets while using speed strafe.");
    }

    @Override
    public void onActivate() {
        resetDirection();
        lastTargetId = -1;
        wasCollidingHorizontally = false;
    }

    @Override
    public void onDeactivate() {
        resetDirection();
        lastTargetId = -1;
        wasCollidingHorizontally = false;
    }

    public boolean shouldStrafe() {
        if (!isActive() || !Utils.canUpdate() || mc.player == null || mc.world == null) return false;
        if (onlyOnJump.get() && !mc.options.jumpKey.isPressed()) return false;
        if (requireInput.get() && !PlayerUtils.isMoving()) return false;

        Speed speed = Modules.get().get(Speed.class);
        if (speed == null || !speed.isActive() || speed.speedMode.get() != SpeedModes.Strafe) return false;

        return getTarget() != null;
    }

    public Vector2d getMovement(double speed, Vector2d fallback) {
        if (!shouldStrafe()) {
            wasCollidingHorizontally = false;
            return fallback;
        }

        boolean collidingHorizontally = mc.player.horizontalCollision;
        if (collidingHorizontally && !wasCollidingHorizontally && autoSwitch.get()) switchDirection();
        wasCollidingHorizontally = collidingHorizontally;

        Vector2d movement = getMovement(speed, currentDirection);
        if (!checkVoid.get() || isSafe(movement)) return movement;

        if (!autoSwitch.get()) return fallback;

        int previousDirection = currentDirection;
        switchDirection();

        Vector2d switchedMovement = getMovement(speed, currentDirection);
        if (isSafe(switchedMovement)) return switchedMovement;

        currentDirection = previousDirection;
        return fallback;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!render.get() || !shouldStrafe()) return;

        Entity target = getTarget();
        if (target == null) return;

        double centerX = MathHelper.lerp(event.tickDelta, target.lastRenderX, target.getX());
        double centerY = target.getBoundingBox().minY + 0.05;
        double centerZ = MathHelper.lerp(event.tickDelta, target.lastRenderZ, target.getZ());

        int segmentCount = segments.get();
        double angleStep = Math.PI * 2.0 / segmentCount;

        for (int i = 0; i < segmentCount; i++) {
            double startAngle = angleStep * i;
            double endAngle = angleStep * (i + 1);

            double startX = centerX + Math.cos(startAngle) * radius.get();
            double startZ = centerZ + Math.sin(startAngle) * radius.get();
            double endX = centerX + Math.cos(endAngle) * radius.get();
            double endZ = centerZ + Math.sin(endAngle) * radius.get();

            event.renderer.line(startX, centerY, startZ, endX, centerY, endZ, lineColor.get());
        }
    }

    private Entity getTarget() {
        KillAura killAura = Modules.get().get(KillAura.class);
        if (killAura == null || !killAura.isActive()) {
            lastTargetId = -1;
            return null;
        }

        Entity target = killAura.getTarget();
        if (target == null || !target.isAlive() || target.isRemoved()) {
            lastTargetId = -1;
            return null;
        }

        if (target.getId() != lastTargetId) {
            lastTargetId = target.getId();
            resetDirection();
        }

        return target;
    }

    private Vector2d getMovement(double speed, int directionSign) {
        Entity target = getTarget();
        if (target == null) return new Vector2d();

        double deltaX = mc.player.getX() - target.getX();
        double deltaZ = mc.player.getZ() - target.getZ();
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (distance < 1.0E-5) {
            deltaX = 1;
            deltaZ = 0;
            distance = 1;
        }

        double normX = deltaX / distance;
        double normZ = deltaZ / distance;

        double tangentX = -normZ * directionSign;
        double tangentZ = normX * directionSign;

        double radialX = 0;
        double radialZ = 0;
        if (distance > radius.get() + RADIUS_TOLERANCE) {
            radialX = -normX * RADIAL_WEIGHT;
            radialZ = -normZ * RADIAL_WEIGHT;
        } else if (distance < radius.get() - RADIUS_TOLERANCE) {
            radialX = normX * RADIAL_WEIGHT;
            radialZ = normZ * RADIAL_WEIGHT;
        }

        double moveX = tangentX + radialX;
        double moveZ = tangentZ + radialZ;
        double length = Math.sqrt(moveX * moveX + moveZ * moveZ);

        if (length < 1.0E-5) return new Vector2d();
        return new Vector2d(moveX / length * speed, moveZ / length * speed);
    }

    private boolean isSafe(Vector2d movement) {
        if (movement.lengthSquared() < 1.0E-5) return true;

        Vec3d normalized = new Vec3d(movement.x, 0, movement.y).normalize();
        Box projectedBox = mc.player.getBoundingBox().offset(normalized.x, 0, normalized.z);
        double supportDepth = Math.max(1.0, mc.player.getStepHeight() + 0.5);

        return !mc.world.isSpaceEmpty(projectedBox.offset(0, -supportDepth, 0));
    }

    private void resetDirection() {
        currentDirection = direction.get().sign;
    }

    private void switchDirection() {
        currentDirection = currentDirection == 0 ? direction.get().sign : -currentDirection;
    }

    public enum Direction {
        Left(1),
        Right(-1);

        private final int sign;

        Direction(int sign) {
            this.sign = sign;
        }
    }
}
