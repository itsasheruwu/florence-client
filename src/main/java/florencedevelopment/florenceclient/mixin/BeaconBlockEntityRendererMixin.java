/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin;

import florencedevelopment.florenceclient.systems.modules.Modules;
import florencedevelopment.florenceclient.systems.modules.render.NoRender;
import net.minecraft.block.entity.BeamEmitter;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.state.BeaconBlockEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntityRenderer.class)
public abstract class BeaconBlockEntityRendererMixin<T extends BlockEntity & BeamEmitter> implements BlockEntityRenderer<T, BeaconBlockEntityRenderState> {
    @Inject(method = "renderBeam(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/util/Identifier;FFIIIFF)V", at = @At("HEAD"), cancellable = true)
    private static void onRender(MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, Identifier textureId, float tickProgress, float heightScale, int i, int j, int k, float f, float g, CallbackInfo ci) {
        if (Modules.get().get(NoRender.class).noBeaconBeams()) ci.cancel();
    }
}
