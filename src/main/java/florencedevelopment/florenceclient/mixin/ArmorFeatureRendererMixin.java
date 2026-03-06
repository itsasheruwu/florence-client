/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin;

import florencedevelopment.florenceclient.systems.modules.Modules;
import florencedevelopment.florenceclient.systems.modules.render.NoRender;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorFeatureRendererMixin<S extends BipedEntityRenderState, M extends BipedEntityModel<S>, A extends BipedEntityModel<S>> {
    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/client/render/entity/state/BipedEntityRenderState;FF)V", at = @At("HEAD"), cancellable = true)
    private void onRender(MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, int i, S bipedEntityRenderState, float f, float g, CallbackInfo ci) {
        if (bipedEntityRenderState instanceof PlayerEntityRenderState && Modules.get().get(NoRender.class).noArmor()) ci.cancel();
    }
}
