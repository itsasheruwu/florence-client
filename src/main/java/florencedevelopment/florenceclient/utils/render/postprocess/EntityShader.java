package florencedevelopment.florenceclient.utils.render.postprocess;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import florencedevelopment.florenceclient.mixininterface.IWorldRenderer;
import florencedevelopment.florenceclient.utils.render.CustomOutlineVertexConsumerProvider;
import net.minecraft.entity.Entity;

import static florencedevelopment.florenceclient.FlorenceClient.mc;

public abstract class EntityShader extends PostProcessShader {
    public final CustomOutlineVertexConsumerProvider vertexConsumerProvider;

    protected EntityShader(RenderPipeline pipeline) {
        super(pipeline);
        this.vertexConsumerProvider = new CustomOutlineVertexConsumerProvider();
    }

    public abstract boolean shouldDraw(Entity entity);

    @Override
    protected void preDraw() {
        ((IWorldRenderer) mc.worldRenderer).florence$pushEntityOutlineFramebuffer(framebuffer);
    }

    @Override
    protected void postDraw() {
        ((IWorldRenderer) mc.worldRenderer).florence$popEntityOutlineFramebuffer();
    }

    public void submitVertices() {
        submitVertices(vertexConsumerProvider::draw);
    }
}
