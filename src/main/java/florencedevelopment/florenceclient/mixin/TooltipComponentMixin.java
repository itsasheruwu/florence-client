/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin;

import florencedevelopment.florenceclient.utils.tooltip.FlorenceTooltipData;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.tooltip.TooltipData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TooltipComponent.class)
public interface TooltipComponentMixin {
    @Inject(method = "of(Lnet/minecraft/item/tooltip/TooltipData;)Lnet/minecraft/client/gui/tooltip/TooltipComponent;", at = @At("HEAD"), cancellable = true)
    private static void shortcutMeteorTooltipData(TooltipData tooltipData, CallbackInfoReturnable<TooltipComponent> cir) {
        if (tooltipData instanceof FlorenceTooltipData) cir.setReturnValue(null);
    }
}
