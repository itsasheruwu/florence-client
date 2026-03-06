/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.render;

import florencedevelopment.florenceclient.events.Cancellable;
import florencedevelopment.florenceclient.mixininterface.IEntityRenderState;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;

public class RenderItemEntityEvent extends Cancellable {
    private static final RenderItemEntityEvent INSTANCE = new RenderItemEntityEvent();

    public ItemEntity itemEntity;
    public ItemEntityRenderState renderState;
    public float tickDelta;
    public MatrixStack matrixStack;
    public VertexConsumerProvider vertexConsumerProvider;
    public int light;
    public ItemModelManager itemModelManager;
    public OrderedRenderCommandQueue renderCommandQueue;

    public static RenderItemEntityEvent get(ItemEntityRenderState renderState, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, ItemModelManager itemModelManager, OrderedRenderCommandQueue renderCommandQueue) {
        INSTANCE.setCancelled(false);
        INSTANCE.itemEntity = (ItemEntity) ((IEntityRenderState) renderState).florence$getEntity();
        INSTANCE.renderState = renderState;
        INSTANCE.tickDelta = tickDelta;
        INSTANCE.matrixStack = matrixStack;
        INSTANCE.vertexConsumerProvider = vertexConsumerProvider;
        INSTANCE.light = light;
        INSTANCE.itemModelManager = itemModelManager;
        INSTANCE.renderCommandQueue = renderCommandQueue;
        return INSTANCE;
    }
}
