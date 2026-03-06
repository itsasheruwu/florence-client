/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import florencedevelopment.florenceclient.FlorenceClient;
import florencedevelopment.florenceclient.events.entity.player.CanWalkOnFluidEvent;
import florencedevelopment.florenceclient.systems.modules.Modules;
import florencedevelopment.florenceclient.systems.modules.movement.HighJump;
import florencedevelopment.florenceclient.systems.modules.movement.Sprint;
import florencedevelopment.florenceclient.systems.modules.movement.elytrafly.ElytraFlightModes;
import florencedevelopment.florenceclient.systems.modules.movement.elytrafly.ElytraFly;
import florencedevelopment.florenceclient.systems.modules.movement.elytrafly.modes.Bounce;
import florencedevelopment.florenceclient.systems.modules.player.NoStatusEffects;
import florencedevelopment.florenceclient.systems.modules.player.OffhandCrash;
import florencedevelopment.florenceclient.systems.modules.render.HandView;
import florencedevelopment.florenceclient.systems.modules.render.NoRender;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static florencedevelopment.florenceclient.FlorenceClient.mc;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyReturnValue(method = "canWalkOnFluid", at = @At("RETURN"))
    private boolean onCanWalkOnFluid(boolean original, FluidState fluidState) {
        if ((Object) this != mc.player) return original;
        CanWalkOnFluidEvent event = FlorenceClient.EVENT_BUS.post(CanWalkOnFluidEvent.get(fluidState));

        return event.walkOnFluid;
    }

    @Inject(method = "spawnItemParticles", at = @At("HEAD"), cancellable = true)
    private void spawnItemParticles(ItemStack stack, int count, CallbackInfo info) {
        NoRender noRender = Modules.get().get(NoRender.class);
        if (noRender.noEatParticles() && stack.getComponents().contains(DataComponentTypes.FOOD)) info.cancel();
    }

    @Inject(method = "onEquipStack", at = @At("HEAD"), cancellable = true)
    private void onEquipStack(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack, CallbackInfo info) {
        if ((Object) this != mc.player) return;

        if (Modules.get().get(OffhandCrash.class).isAntiCrash()) {
            info.cancel();
        }
    }

    @ModifyArg(method = "swingHand(Lnet/minecraft/util/Hand;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;swingHand(Lnet/minecraft/util/Hand;Z)V"))
    private Hand setHand(Hand hand) {
        if ((Object) this != mc.player) return hand;

        HandView handView = Modules.get().get(HandView.class);
        if (handView.isActive()) {
            if (handView.swingMode.get() == HandView.SwingMode.None) return hand;
            return handView.swingMode.get() == HandView.SwingMode.Offhand ? Hand.OFF_HAND : Hand.MAIN_HAND;
        }
        return hand;
    }

    @ModifyExpressionValue(method = "getHandSwingDuration", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/SwingAnimationComponent;duration()I"))
    private int getHandSwingDuration(int original) {
        if ((Object) this != mc.player) return original;

        return Modules.get().get(HandView.class).isActive() && mc.options.getPerspective().isFirstPerson() ? Modules.get().get(HandView.class).swingSpeed.get() : original;
    }

    @ModifyReturnValue(method = "isGliding", at = @At("RETURN"))
    private boolean isGlidingHook(boolean original) {
        if ((Object) this != mc.player) return original;

        if (Modules.get().get(ElytraFly.class).canPacketEfly()) {
            return true;
        }

        return original;
    }

    @Unique
    private boolean previousElytra = false;

    @Inject(method = "isGliding", at = @At("TAIL"), cancellable = true)
    public void recastOnLand(CallbackInfoReturnable<Boolean> cir) {
        boolean elytra = cir.getReturnValue();
        ElytraFly elytraFly = Modules.get().get(ElytraFly.class);
        if (previousElytra && !elytra && elytraFly.isActive() && elytraFly.flightMode.get() == ElytraFlightModes.Bounce) {
            cir.setReturnValue(Bounce.recastElytra(mc.player));
        }
        previousElytra = elytra;
    }

    @ModifyReturnValue(method = "hasStatusEffect", at = @At("RETURN"))
    private boolean hasStatusEffect(boolean original, RegistryEntry<StatusEffect> effect) {
        if (effect == null || effect.value() == null) return original;
        if (Modules.get().get(NoStatusEffects.class).shouldBlock(effect.value())) return false;

        return original;
    }

    @ModifyExpressionValue(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"))
    private float modifyGetYaw(float original) {
        if ((Object) this != mc.player) return original;
        if (!Modules.get().get(Sprint.class).rageSprint()) return original;

        float forward = Math.signum(mc.player.forwardSpeed);
        float strafe = 90 * Math.signum(mc.player.sidewaysSpeed);
        if (forward != 0) strafe *= (forward * 0.5f);

        original -= strafe;
        if (forward < 0) original -= 180;

        return original;
    }

    @ModifyConstant(method = "jump", constant = @Constant(floatValue = 1.0E-5F))
    private float modifyJumpConstant(float original) {
        if ((Object) this != mc.player) return original;
        if (!Modules.get().isActive(HighJump.class)) return original;
        return -1;
    }

    @ModifyExpressionValue(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSprinting()Z"))
    private boolean modifyIsSprinting(boolean original) {
        if ((Object) this != mc.player) return original;
        if (!Modules.get().get(Sprint.class).rageSprint()) return original;

        // only add the extra velocity if you're actually moving, otherwise you'll jump in place and move forward
        return original && (Math.abs(mc.player.forwardSpeed) > 1.0E-5F || Math.abs(mc.player.sidewaysSpeed) > 1.0E-5F);
    }
}
