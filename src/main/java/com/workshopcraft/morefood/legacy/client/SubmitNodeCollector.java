package com.workshopcraft.morefood.legacy.client;

//? if <26.1 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/^* Stands in for the 26.1 submit collector by drawing straight into the buffers. ^/
public final class SubmitNodeCollector {
    public interface CustomGeometryRenderer {
        void render(PoseStack.Pose pose, VertexConsumer buffer);
    }

    private final MultiBufferSource buffers;

    public SubmitNodeCollector(MultiBufferSource buffers) {
        this.buffers = buffers;
    }

    public MultiBufferSource buffers() {
        return buffers;
    }

    public void submitCustomGeometry(PoseStack pose, RenderType type, CustomGeometryRenderer renderer) {
        renderer.render(pose.last(), buffers.getBuffer(type));
    }
}
*///?}
