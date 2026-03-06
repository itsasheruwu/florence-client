/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import florencedevelopment.florenceclient.FlorenceClient;
import florencedevelopment.florenceclient.events.entity.EntityMoveEvent;
import florencedevelopment.florenceclient.events.entity.player.JumpVelocityMultiplierEvent;
import florencedevelopment.florenceclient.events.entity.player.PlayerMoveEvent;
import florencedevelopment.florenceclient.mixininterface.ICamera;
import florencedevelopment.florenceclient.systems.modules.Modules;
import florencedevelopment.florenceclient.systems.modules.combat.Hitboxes;
import florencedevelopment.florenceclient.systems.modules.movement.*;
import florencedevelopment.florenceclient.systems.modules.movement.elytrafly.ElytraFly;
import florencedevelopment.florenceclient.systems.modules.movement.Languid;
import florencedevelopment.florenceclient.systems.modules.render.ESP;
import florencedevelopment.florenceclient.systems.modules.render.FreeLook;
import florencedevelopment.florenceclient.systems.modules.render.Freecam;
import florencedevelopment.florenceclient.systems.modules.render.NoRender;
import florencedevelopment.florenceclient.systems.modules.world.HighwayBuilder;
import florencedevelopment.florenceclient.utils.Utils;
import florencedevelopment.florenceclient.utils.entity.fakeplayer.FakePlayerEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static florencedevelopment.florenceclient.FlorenceClient.mc;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @ModifyExpressionValue(method = "updateMovementInFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;getVelocity(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d updateMovementInFluidFluidStateGetVelocity(Vec3d vec) {
        if ((Object) this != mc.player) return vec;

        Velocity velocity = Modules.get().get(Velocity.class);
        if (velocity.isActive() && velocity.liquids.get()) {
            vec = vec.multiply(velocity.getHorizontal(velocity.liquidsHorizontal), velocity.getVertical(velocity.liquidsVertical), velocity.getHorizontal(velocity.liquidsHorizontal));
        }

        return vec;
    }

    @Inject(method = "isTouchingWater", at = @At(value = "HEAD"), cancellable = true)
    private void isTouchingWater(CallbackInfoReturnable<Boolean> info) {
        if ((Object) this != mc.player) return;

        if (Modules.get().get(Flight.class).isActive()) info.setReturnValue(false);
        if (Modules.get().get(NoSlow.class).fluidDrag()) info.setReturnValue(false);
    }

    @Inject(method = "isInLava", at = @At(value = "HEAD"), cancellable = true)
    private void isInLava(CallbackInfoReturnable<Boolean> info) {
        if ((Object) this != mc.player) return;

        if (Modules.get().get(Flight.class).isActive()) info.setReturnValue(false);
        if (Modules.get().get(NoSlow.class).fluidDrag()) info.setReturnValue(false);
    }

    @Inject(method = "onBubbleColumnSurfaceCollision", at = @At("HEAD"))
    private void onBubbleColumnSurfaceCollision(CallbackInfo info) {
        if ((Object) this != mc.player) return;

        Jesus jesus = Modules.get().get(Jesus.class);
        if (jesus.isActive()) {
            jesus.isInBubbleColumn = true;
        }
    }

    @Inject(method = "onBubbleColumnCollision", at = @At("HEAD"))
    private void onBubbleColumnCollision(CallbackInfo info) {
        if ((Object) this != mc.player) return;

        Jesus jesus = Modules.get().get(Jesus.class);
        if (jesus.isActive()) {
            jesus.isInBubbleColumn = true;
        }
    }

    @ModifyExpressionValue(method = "updateSwimming", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSubmergedInWater()Z"))
    private boolean isSubmergedInWater(boolean submerged) {
        if ((Object) this != mc.player) return submerged;

        if (Modules.get().get(NoSlow.class).fluidDrag()) return false;
        if (Modules.get().get(Flight.class).isActive()) return false;
        return submerged;
    }

    @ModifyArgs(method = "pushAwayFrom(Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    private void onPushAwayFrom(Args args, Entity entity) {
        Velocity velocity = Modules.get().get(Velocity.class);

        // Velocity
        if ((Object) this == mc.player && velocity.isActive() && velocity.entityPush.get()) {
            double multiplier = velocity.entityPushAmount.get();
            args.set(0, (double) args.get(0) * multiplier);
            args.set(2, (double) args.get(2) * multiplier);
        }
        // FakePlayerEntity
        else if (entity instanceof FakePlayerEntity player && player.doNotPush) {
            args.set(0, 0.0);
            args.set(2, 0.0);
        }
    }

    @ModifyReturnValue(method = "getJumpVelocityMultiplier", at = @At("RETURN"))
    private float onGetJumpVelocityMultiplier(float original) {
        if ((Object) this == mc.player) {
            JumpVelocityMultiplierEvent event = FlorenceClient.EVENT_BUS.post(JumpVelocityMultiplierEvent.get());
            return (original * event.multiplier);
        }

        return original;
    }

    @Inject(method = "move", at = @At("HEAD"))
    private void onMove(MovementType type, Vec3d movement, CallbackInfo info) {
        if ((Object) this == mc.player) {
            FlorenceClient.EVENT_BUS.post(PlayerMoveEvent.get(type, movement));
        }
        else {
            FlorenceClient.EVENT_BUS.post(EntityMoveEvent.get((Entity) (Object) this, movement));
        }
    }

    @ModifyExpressionValue(method = "getVelocityMultiplier", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;"))
    private Block modifyVelocityMultiplierBlock(Block original) {
        if ((Object) this != mc.player) return original;

        Languid languid = Modules.get().get(Languid.class);
        NoSlow noSlow = Modules.get().get(NoSlow.class);
        
        // Handle Languid Replace mode (replaces soul sand/soul soil with stone, like NoSlow)
        if (languid != null && languid.replaceMode()) {
            if (original == Blocks.SOUL_SAND && languid.soulSand.get()) return Blocks.STONE;
            if (original == Blocks.SOUL_SOIL && languid.soulSoil.get()) return Blocks.STONE;
        }
        
        // Don't apply NoSlow's soul sand bypass if Languid is active in Emulate mode
        if (languid != null && languid.isActive() && languid.emulateMode() && (original == Blocks.SOUL_SAND || original == Blocks.SOUL_SOIL)) {
            return original; // Let Languid handle it via getVelocityMultiplier return value modification
        }

        // Don't apply NoSlow's soul sand bypass if Languid is handling it in Replace mode
        if (languid != null && languid.isActive() && languid.replaceMode() && (original == Blocks.SOUL_SAND || original == Blocks.SOUL_SOIL)) {
            return original; // Already handled above
        }
        if (original == Blocks.HONEY_BLOCK && noSlow.honeyBlock()) return Blocks.STONE;
        return original;
    }

    @ModifyReturnValue(method = "getVelocityMultiplier", at = @At("RETURN"))
    private float modifyVelocityMultiplierReturn(float original) {
        if ((Object) this != mc.player) return original;
        if (!Utils.canUpdate()) return original;

        Languid languid = Modules.get().get(Languid.class);
        if (languid == null || !languid.emulateMode()) return original;

        // Check if player is on soul sand or soul soil
        BlockPos pos = mc.player.getBlockPos().down();
        Block block = mc.world.getBlockState(pos).getBlock();
        
        if (block == Blocks.SOUL_SAND && languid.soulSand.get()) {
            // Apply Soul Speed III multiplier: 0.9 + (3 × 0.125) = 1.275
            // Soul sand has base multiplier of 0.4, so with Soul Speed III: 0.4 * 1.275 = 0.51
            return (float) (original * 1.275);
        }
        
        if (block == Blocks.SOUL_SOIL && languid.soulSoil.get()) {
            // Soul soil doesn't slow down (multiplier is 1.0), but apply Soul Speed III bonus anyway
            // This gives a speed boost even on soul soil
            return (float) (original * 1.275);
        }

        return original;
    }

    @ModifyReturnValue(method = "isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z", at = @At("RETURN"))
    private boolean isInvisibleToCanceller(boolean original) {
        if (!Utils.canUpdate()) return original;
        ESP esp = Modules.get().get(ESP.class);
        if (Modules.get().get(NoRender.class).noInvisibility() || esp.isActive() && !esp.shouldSkip((Entity) (Object) this)) return false;
        return original;
    }

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void isGlowing(CallbackInfoReturnable<Boolean> info) {
        if (Modules.get().get(NoRender.class).noGlowing()) info.setReturnValue(false);
    }

    @Inject(method = "getTargetingMargin", at = @At("HEAD"), cancellable = true)
    private void onGetTargetingMargin(CallbackInfoReturnable<Float> info) {
        double v = Modules.get().get(Hitboxes.class).getEntityValue((Entity) (Object) this);
        if (v != 0) info.setReturnValue((float) v);
    }

    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    private void onIsInvisibleTo(PlayerEntity player, CallbackInfoReturnable<Boolean> info) {
        if (player == null) info.setReturnValue(false);
    }

    @Inject(method = "getPose", at = @At("HEAD"), cancellable = true)
    private void getPoseHook(CallbackInfoReturnable<EntityPose> info) {
        if ((Object) this != mc.player) return;

        if (Modules.get().get(ElytraFly.class).canPacketEfly()) {
            info.setReturnValue(EntityPose.GLIDING);
        }
    }

    @ModifyReturnValue(method = "getPose", at = @At("RETURN"))
    private EntityPose modifyGetPose(EntityPose original) {
        if ((Object) this != mc.player) return original;

        if (original == EntityPose.CROUCHING && !mc.player.isSneaking() && ((PlayerEntityAccessor) mc.player).florence$canChangeIntoPose(EntityPose.STANDING)) return EntityPose.STANDING;
        return original;
    }

    @ModifyReturnValue(method = "bypassesLandingEffects", at = @At("RETURN"))
    private boolean cancelBounce(boolean original) {
        return Modules.get().get(NoFall.class).cancelBounce() || original;
    }

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void updateChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if ((Object) this != mc.player) return;

        Freecam freecam = Modules.get().get(Freecam.class);
        FreeLook freeLook = Modules.get().get(FreeLook.class);

        if (freecam.isActive()) {
            freecam.changeLookDirection(cursorDeltaX * 0.15, cursorDeltaY * 0.15);
            ci.cancel();
        }
        else if (Modules.get().isActive(HighwayBuilder.class)) {
            Camera camera = mc.gameRenderer.getCamera();
            ((ICamera) camera).florence$setRot(camera.getYaw() + cursorDeltaX * 0.15, camera.getPitch() + cursorDeltaY * 0.15);
            ci.cancel();
        }
        else if (freeLook.cameraMode()) {
            freeLook.cameraYaw += (float) (cursorDeltaX / freeLook.sensitivity.get().floatValue());
            freeLook.cameraPitch += (float) (cursorDeltaY / freeLook.sensitivity.get().floatValue());

            if (Math.abs(freeLook.cameraPitch) > 90.0F) freeLook.cameraPitch = freeLook.cameraPitch > 0.0F ? 90.0F : -90.0F;
            ci.cancel();
        }
    }
}
