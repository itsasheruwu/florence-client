/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import florencedevelopment.florenceclient.FlorenceClient;
import florencedevelopment.florenceclient.events.entity.player.FinishUsingItemEvent;
import florencedevelopment.florenceclient.events.entity.player.StoppedUsingItemEvent;
import florencedevelopment.florenceclient.events.game.ItemStackTooltipEvent;
import florencedevelopment.florenceclient.utils.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static florencedevelopment.florenceclient.FlorenceClient.mc;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @ModifyReturnValue(method = "getTooltip", at = @At("RETURN"))
    private List<Text> onGetTooltip(List<Text> original) {
        if (Utils.canUpdate()) {
            ItemStackTooltipEvent event = FlorenceClient.EVENT_BUS.post(new ItemStackTooltipEvent((ItemStack) (Object) this, original));
            return event.list();
        }

        return original;
    }

    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void onFinishUsing(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> info) {
        if (user == mc.player) {
            FlorenceClient.EVENT_BUS.post(FinishUsingItemEvent.get((ItemStack) (Object) this));
        }
    }

    @Inject(method = "onStoppedUsing", at = @At("HEAD"))
    private void onStoppedUsing(World world, LivingEntity user, int remainingUseTicks, CallbackInfo info) {
        if (user == mc.player) {
            FlorenceClient.EVENT_BUS.post(StoppedUsingItemEvent.get((ItemStack) (Object) this));
        }
    }
}
