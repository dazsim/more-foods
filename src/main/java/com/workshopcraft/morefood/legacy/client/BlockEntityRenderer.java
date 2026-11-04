package com.workshopcraft.morefood.legacy.client;

//? if <26.1 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/^* Runs a 26.1 style extract and submit renderer from the older single render call. ^/
public interface BlockEntityRenderer<T extends BlockEntity, S extends BlockEntityRenderState> extends net.minecraft.client.renderer.blockentity.BlockEntityRenderer<T> {
    S createRenderState();

    default void extractRenderState(T blockEntity, S state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
    }

    void submit(S state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera);

    @Override
    default void render(T blockEntity, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        final S state = createRenderState();
        state.lightCoords = light;
        extractRenderState(blockEntity, state, partialTicks, Vec3.ZERO, null);
        submit(state, pose, new SubmitNodeCollector(buffers), new CameraRenderState());
    }
}
*///?}
