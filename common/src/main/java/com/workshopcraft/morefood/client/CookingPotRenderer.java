package com.workshopcraft.morefood.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.workshopcraft.morefood.stove.CookingPotBlockEntity;
import com.workshopcraft.morefood.stove.HobTool;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class CookingPotRenderer implements BlockEntityRenderer<CookingPotBlockEntity, CookingPotRenderState> {
    private final SpriteGetter sprites;

    public CookingPotRenderer(BlockEntityRendererProvider.Context context) {
        sprites = context.sprites();
    }

    @Override
    public CookingPotRenderState createRenderState() {
        return new CookingPotRenderState();
    }

    @Override
    public void extractRenderState(CookingPotBlockEntity pot, CookingPotRenderState state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(pot, state, partialTicks, cameraPosition, breakProgress);
        final Level level = pot.getLevel();
        state.time = (level != null ? level.getGameTime() % 24000 : 0) + partialTicks;
        state.stew = sprites.get(StoveRenderer.STEW);
        state.waterSprite = sprites.get(StoveRenderer.WATER);
        state.pot.tool = HobTool.POT;
        StoveRenderer.extractPot(pot.pot(), state.pot, pot.getBlockPos().asLong(), sprites);
    }

    @Override
    public void submit(CookingPotRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        final float surface = CookingPotBlockEntity.liquidHeight(state.pot.surface);
        StoveRenderer.submitPot(state.stew, state.waterSprite, state.time, state.lightCoords, state.pot, 8, 8, 4, 4, surface, 1.2F, pose, collector);
    }
}
