/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin;

import florencedevelopment.florenceclient.FlorenceClient;
import florencedevelopment.florenceclient.events.render.RenderItemEntityEvent;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static florencedevelopment.florenceclient.FlorenceClient.mc;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {
    @Shadow
    @Final
    private ItemModelManager itemModelManager;

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void renderStack(ItemEntityRenderState itemEntityRenderState, MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, CameraRenderState arg, CallbackInfo ci) {
        RenderItemEntityEvent event = FlorenceClient.EVENT_BUS.post(RenderItemEntityEvent.get(itemEntityRenderState, mc.getRenderTickCounter().getTickProgress(true), matrixStack, null, itemEntityRenderState.light, this.itemModelManager, orderedRenderCommandQueue));
        if (event.isCancelled()) ci.cancel();
    }
}
