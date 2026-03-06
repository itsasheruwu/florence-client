/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.modules.movement;

import com.google.common.collect.Streams;
import florencedevelopment.florenceclient.events.entity.player.CanWalkOnFluidEvent;
import florencedevelopment.florenceclient.events.packets.PacketEvent;
import florencedevelopment.florenceclient.events.world.CollisionShapeEvent;
import florencedevelopment.florenceclient.events.world.TickEvent;
import florencedevelopment.florenceclient.mixin.LivingEntityAccessor;
import florencedevelopment.florenceclient.mixininterface.IVec3d;
import florencedevelopment.florenceclient.pathing.PathManagers;
import florencedevelopment.florenceclient.settings.*;
import florencedevelopment.florenceclient.systems.modules.Categories;
import florencedevelopment.florenceclient.systems.modules.Module;
import florencedevelopment.florenceclient.systems.modules.Modules;
import florencedevelopment.florenceclient.utils.entity.EntityUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Jesus extends Module {
    private final SettingGroup sgGeneral = settings.createGroup("General");

    // General

    private final Setting<List<Block>> affectedBlocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("affected-blocks")
        .description("Which blocks Jesus should affect.")
        .defaultValue(Blocks.WATER, Blocks.LAVA, Blocks.POWDER_SNOW)
        .filter(block -> block == Blocks.WATER || block == Blocks.LAVA || block == Blocks.POWDER_SNOW)
        .build()
    );

    // Water

    private final Setting<Mode> waterMode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("water-mode")
        .description("How to treat the water.")
        .defaultValue(Mode.Solid)
        .visible(this::affectsWater)
        .build()
    );

    private final Setting<Boolean> dipIfBurning = sgGeneral.add(new BoolSetting.Builder()
        .name("water-dip-if-burning")
        .description("Lets you go into the water when you are burning.")
        .defaultValue(true)
        .visible(() -> affectsWater() && waterMode.get() == Mode.Solid)
        .build()
    );

    private final Setting<Boolean> dipOnSneakWater = sgGeneral.add(new BoolSetting.Builder()
        .name("water-dip-on-sneak")
        .description("Lets you go into the water when your sneak key is held.")
        .defaultValue(true)
        .visible(() -> affectsWater() && waterMode.get() == Mode.Solid)
        .build()
    );

    private final Setting<Boolean> dipOnFallWater = sgGeneral.add(new BoolSetting.Builder()
        .name("water-dip-on-fall")
        .description("Lets you go into the water when you fall over a certain height.")
        .defaultValue(true)
        .visible(() -> affectsWater() && waterMode.get() == Mode.Solid)
        .build()
    );

    private final Setting<Integer> dipFallHeightWater = sgGeneral.add(new IntSetting.Builder()
        .name("water-dip-fall-height")
        .description("The fall height at which you will go into the water.")
        .defaultValue(4)
        .range(1, 255)
        .sliderRange(3, 20)
        .visible(() -> affectsWater() && waterMode.get() == Mode.Solid && dipOnFallWater.get())
        .build()
    );

    // Lava

    private final Setting<Mode> lavaMode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("lava-mode")
        .description("How to treat the lava.")
        .defaultValue(Mode.Solid)
        .visible(this::affectsLava)
        .build()
    );

    private final Setting<Boolean> dipIfFireResistant = sgGeneral.add(new BoolSetting.Builder()
        .name("lava-dip-if-resistant")
        .description("Lets you go into the lava if you have Fire Resistance effect.")
        .defaultValue(true)
        .visible(() -> affectsLava() && lavaMode.get() == Mode.Solid)
        .build()
    );

    private final Setting<Boolean> dipOnSneakLava = sgGeneral.add(new BoolSetting.Builder()
        .name("lava-dip-on-sneak")
        .description("Lets you go into the lava when your sneak key is held.")
        .defaultValue(true)
        .visible(() -> affectsLava() && lavaMode.get() == Mode.Solid)
        .build()
    );

    private final Setting<Boolean> dipOnFallLava = sgGeneral.add(new BoolSetting.Builder()
        .name("lava-dip-on-fall")
        .description("Lets you go into the lava when you fall over a certain height.")
        .defaultValue(true)
        .visible(() -> affectsLava() && lavaMode.get() == Mode.Solid)
        .build()
    );

    private final Setting<Integer> dipFallHeightLava = sgGeneral.add(new IntSetting.Builder()
        .name("lava-dip-fall-height")
        .description("The fall height at which you will go into the lava.")
        .defaultValue(4)
        .range(1, 255)
        .sliderRange(3, 20)
        .visible(() -> affectsLava() && lavaMode.get() == Mode.Solid && dipOnFallLava.get())
        .build()
    );

    // Other

    private final BlockPos.Mutable blockPos = new BlockPos.Mutable();

    private int tickTimer = 10;
    private int packetTimer = 0;

    private boolean prePathManagerWalkOnWater;
    private boolean prePathManagerWalkOnLava;

    public boolean isInBubbleColumn = false;

    public Jesus() {
        super(Categories.Movement, "jesus", "Walk on liquids and powder snow like Jesus.");
    }

    @Override
    public void onActivate() {
        prePathManagerWalkOnWater = PathManagers.get().getSettings().getWalkOnWater().get();
        prePathManagerWalkOnLava = PathManagers.get().getSettings().getWalkOnLava().get();

        PathManagers.get().getSettings().getWalkOnWater().set(affectsWater() && waterMode.get() == Mode.Solid);
        PathManagers.get().getSettings().getWalkOnLava().set(affectsLava() && lavaMode.get() == Mode.Solid);
    }

    @Override
    public void onDeactivate() {
        PathManagers.get().getSettings().getWalkOnWater().set(prePathManagerWalkOnWater);
        PathManagers.get().getSettings().getWalkOnLava().set(prePathManagerWalkOnLava);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        boolean bubbleColumn = isInBubbleColumn;
        isInBubbleColumn = false;

        if ((affectsWater() && waterMode.get() == Mode.Bob && mc.player.isTouchingWater()) || (affectsLava() && lavaMode.get() == Mode.Bob && mc.player.isInLava())) {
            double fluidHeight;
            if (mc.player.isInLava()) fluidHeight = mc.player.getFluidHeight(FluidTags.LAVA);
            else fluidHeight = mc.player.getFluidHeight(FluidTags.WATER);

            double swimHeight = mc.player.getSwimHeight();

            if (mc.player.isTouchingWater() && fluidHeight > swimHeight) {
                ((LivingEntityAccessor) mc.player).florence$swimUpwards(FluidTags.WATER);
            } else if (mc.player.isOnGround() && fluidHeight <= swimHeight && ((LivingEntityAccessor) mc.player).florence$getJumpCooldown() == 0) {
                mc.player.jump();
                ((LivingEntityAccessor) mc.player).florence$setJumpCooldown(10);
            } else {
                ((LivingEntityAccessor) mc.player).florence$swimUpwards(FluidTags.LAVA);
            }
        }

        if (mc.player.isTouchingWater() && !waterShouldBeSolid()) return;
        if (mc.player.isInSwimmingPose()) return;
        if (mc.player.isInLava() && !lavaShouldBeSolid()) return;

        // Move up in bubble columns
        if (bubbleColumn) {
            if (mc.options.jumpKey.isPressed() && mc.player.getVelocity().getY() < 0.11) ((IVec3d) mc.player.getVelocity()).florence$setY(0.11);
            return;
        }

        // Move up
        if (mc.player.isTouchingWater() || mc.player.isInLava()) {
            ((IVec3d) mc.player.getVelocity()).florence$setY(0.11);
            tickTimer = 0;
            return;
        }

        BlockState blockBelowState = mc.world.getBlockState(mc.player.getBlockPos().down());
        boolean waterLogger = false;
        try {
            waterLogger = blockBelowState.get(Properties.WATERLOGGED);
        } catch (Exception ignored) {}


        // Simulate jumping out of water
        if (tickTimer == 0) ((IVec3d) mc.player.getVelocity()).florence$setY(0.30);
        else if (tickTimer == 1 && (blockBelowState == Blocks.WATER.getDefaultState() || blockBelowState == Blocks.LAVA.getDefaultState() || waterLogger))
            ((IVec3d) mc.player.getVelocity()).florence$setY(0);

        tickTimer++;
    }

    @EventHandler
    private void onCanWalkOnFluid(CanWalkOnFluidEvent event) {
        if (mc.player != null && mc.player.isSwimming()) return;
        if ((event.fluidState.getFluid() == Fluids.WATER || event.fluidState.getFluid() == Fluids.FLOWING_WATER) && waterShouldBeSolid()) {
            event.walkOnFluid = true;
        }
        else if ((event.fluidState.getFluid() == Fluids.LAVA || event.fluidState.getFluid() == Fluids.FLOWING_LAVA) && lavaShouldBeSolid()) {
            event.walkOnFluid = true;
        }
    }

    @EventHandler
    private void onFluidCollisionShape(CollisionShapeEvent event) {
        if (event.state.getBlock() == Blocks.POWDER_SNOW && canWalkOnPowderSnow() && event.pos.getY() <= mc.player.getY() - 1) {
            event.shape = VoxelShapes.fullCube();
            return;
        }

        if (event.state.getFluidState().isEmpty()) return;

        if ((event.state.getBlock() == Blocks.WATER | event.state.getFluidState().getFluid() == Fluids.WATER) && !mc.player.isTouchingWater() && waterShouldBeSolid() && event.pos.getY() <= mc.player.getY() - 1) {
            event.shape = VoxelShapes.fullCube();
        } else if (event.state.getBlock() == Blocks.LAVA && !mc.player.isInLava() && lavaShouldBeSolid() && (!lavaIsSafe() || event.pos.getY() <= mc.player.getY() - 1)) {
            event.shape = VoxelShapes.fullCube();
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (!(event.packet instanceof PlayerMoveC2SPacket packet)) return;
        if (mc.player.isTouchingWater() && !waterShouldBeSolid()) return;
        if (mc.player.isInLava() && !lavaShouldBeSolid()) return;

        // Check if packet contains a position
        if (!(packet instanceof PlayerMoveC2SPacket.PositionAndOnGround || packet instanceof PlayerMoveC2SPacket.Full)) return;

        // Check inWater, fallDistance and if over liquid
        if (mc.player.isTouchingWater() || mc.player.isInLava() || mc.player.fallDistance > 3f || !isOverLiquid()) return;

        // If not actually moving, cancel packet
        if (mc.player.input.getMovementInput().equals(Vec2f.ZERO)) {
            event.cancel();
            return;
        }

        // Wait for timer
        if (packetTimer++ < 4) return;
        packetTimer = 0;

        // Cancel old packet
        event.cancel();

        // Get position
        double x = packet.getX(0);
        double y = packet.getY(0) + 0.05;
        double z = packet.getZ(0);

        // Create new packet
        Packet<?> newPacket;
        if (packet instanceof PlayerMoveC2SPacket.PositionAndOnGround) {
            newPacket = new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true, mc.player.horizontalCollision);
        }
        else {
            newPacket = new PlayerMoveC2SPacket.Full(x, y, z, packet.getYaw(0), packet.getPitch(0), true, mc.player.horizontalCollision);
        }

        // Send new packet
        mc.getNetworkHandler().getConnection().send(newPacket);
    }

    private boolean waterShouldBeSolid() {
        if (!affectsWater()) return false;
        if (EntityUtils.getGameMode(mc.player) == GameMode.SPECTATOR || mc.player.getAbilities().flying) return false;

        if (mc.player.getVehicle() != null) {
            Entity vehicle = mc.player.getVehicle();
            if (vehicle instanceof AbstractBoatEntity) return false;
        }

        if (Modules.get().get(Flight.class).isActive()) return false;

        if (dipIfBurning.get() && mc.player.isOnFire()) return false;

        if (dipOnSneakWater.get() && mc.options.sneakKey.isPressed()) return false;
        if (dipOnFallWater.get() && mc.player.fallDistance > dipFallHeightWater.get()) return false;

        return waterMode.get() == Mode.Solid;
    }

    private boolean lavaShouldBeSolid() {
        if (!affectsLava()) return false;
        if (EntityUtils.getGameMode(mc.player) == GameMode.SPECTATOR || mc.player.getAbilities().flying) return false;

        if (!lavaIsSafe() && lavaMode.get() == Mode.Solid) return true;

        if (dipOnSneakLava.get() && mc.options.sneakKey.isPressed()) return false;
        if (dipOnFallLava.get() && mc.player.fallDistance > dipFallHeightLava.get()) return false;

        return lavaMode.get() == Mode.Solid;
    }

    private boolean lavaIsSafe() {
        if (!dipIfFireResistant.get()) return false;
        return mc.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE) && (mc.player.getStatusEffect(StatusEffects.FIRE_RESISTANCE).getDuration() > (15 * 20 * mc.player.getAttributeValue(EntityAttributes.BURNING_TIME)));
    }

    private boolean isOverLiquid() {
        boolean foundLiquid = false;
        boolean foundSolid = false;

        List<Box> blockCollisions = Streams.stream(mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0, -0.5, 0)))
            .map(VoxelShape::getBoundingBox)
            .collect(Collectors.toCollection(ArrayList::new));

        for (Box bb : blockCollisions) {
            blockPos.set(MathHelper.lerp(0.5D, bb.minX, bb.maxX), MathHelper.lerp(0.5D, bb.minY, bb.maxY), MathHelper.lerp(0.5D, bb.minZ, bb.maxZ));
            BlockState blockState = mc.world.getBlockState(blockPos);

            if (affectsWater() && (blockState.getBlock() == Blocks.WATER | blockState.getFluidState().getFluid() == Fluids.WATER))
                foundLiquid = true;
            else if (affectsLava() && blockState.getBlock() == Blocks.LAVA)
                foundLiquid = true;
            else if (!blockState.isAir()) foundSolid = true;
        }

        return foundLiquid && !foundSolid;
    }

    public enum Mode {
        Solid,
        Bob,
        Ignore
    }

    private boolean affectsWater() {
        return affectedBlocks.get().contains(Blocks.WATER);
    }

    private boolean affectsLava() {
        return affectedBlocks.get().contains(Blocks.LAVA);
    }

    private boolean affectsPowderSnow() {
        return affectedBlocks.get().contains(Blocks.POWDER_SNOW);
    }

    public boolean canWalkOnPowderSnow() {
        return isActive() && affectsPowderSnow();
    }
}
