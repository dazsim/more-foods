package com.workshopcraft.morefood.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.workshopcraft.morefood.board.AssemblyBoardBlock;
import com.workshopcraft.morefood.board.AssemblyBoardBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class AssemblyBoardRenderer implements BlockEntityRenderer<AssemblyBoardBlockEntity, AssemblyBoardRenderState> {
    private static final float BOARD_TOP = 2;
    private static final float MAX_TOP = 13.5F;
    private final ItemModelResolver itemModelResolver;
    private final SpriteGetter sprites;

    public AssemblyBoardRenderer(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
        sprites = context.sprites();
    }

    @Override
    public AssemblyBoardRenderState createRenderState() {
        return new AssemblyBoardRenderState();
    }

    @Override
    public void extractRenderState(AssemblyBoardBlockEntity board, AssemblyBoardRenderState state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(board, state, partialTicks, cameraPosition, breakProgress);
        state.facing = board.getBlockState().getValue(AssemblyBoardBlock.FACING);
        state.complete = board.isComplete();
        state.layers.clear();
        final List<ItemStack> layers = board.layers();
        final long seed = board.getBlockPos().asLong();
        for (int i = 0; i < layers.size(); i++) {
            final ItemStack stack = layers.get(i);
            final AssemblyBoardRenderState.Layer layer = new AssemblyBoardRenderState.Layer();
            layer.seed = seed * 31 + i * 7919L + BuiltInRegistries.ITEM.getKey(stack.getItem()).hashCode();
            layer.spec = BoardLayers.get(BuiltInRegistries.ITEM.getKey(stack.getItem()));
            if (layer.spec != null) {
                layer.sprite = sprites.get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, layer.spec.texture()));
                layer.top = layer.spec.top().map(id -> sprites.get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, id))).orElse(null);
            } else {
                itemModelResolver.updateForTopItem(layer.item, stack, ItemDisplayContext.FIXED, board.getLevel(), null, (int) layer.seed);
            }
            state.layers.add(layer);
        }
    }

    @Override
    public void submit(AssemblyBoardRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.layers.isEmpty()) return;
        pose.pushPose();
        pose.translate(0.5F, 0, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot() + 180));
        pose.translate(-0.5F, 0, -0.5F);
        final int light = state.lightCoords;
        float y = BOARD_TOP;
        AssemblyBoardRenderState.Layer bun = null;
        for (AssemblyBoardRenderState.Layer layer : state.layers) {
            final BoardLayers.Spec spec = layer.spec;
            if (spec == null || layer.sprite == null) {
                pose.pushPose();
                pose.translate(0.5F, (y + 0.3F) / 16, 0.5F);
                pose.mulPose(Axis.XP.rotationDegrees(90));
                pose.scale(0.6F, 0.6F, 0.6F);
                layer.item.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, 0);
                pose.popPose();
                y += 1;
            } else {
                final TextureAtlasSprite sprite = layer.sprite;
                final float t = spec.thickness(), s = spec.size(), y0 = y;
                switch (spec.shape()) {
                    case DISC -> disc(pose, collector, s, y0, y0 + t, sprite, light);
                    case SQUARE -> box(pose, collector, 8 - s / 2, y0, 8 - s / 2, 8 + s / 2, y0 + t, 8 + s / 2, sprite, light);
                    case LEAF -> leaf(pose, collector, s, y0, y0 + t, sprite, light, layer.seed);
                    case BITS -> bits(pose, collector, spec.count(), y0, sprite, light, layer.seed);
                    case BUN -> {
                        disc(pose, collector, s, y0, y0 + t, sprite, light);
                        if (bun == null) bun = layer;
                    }
                }
                y += t;
            }
            if (y > MAX_TOP) break;
        }
        if (state.complete && bun != null && bun.spec != null) {
            final TextureAtlasSprite top = bun.top != null ? bun.top : bun.sprite;
            final float s = bun.spec.size();
            disc(pose, collector, s, y, y + 1.5F, top, light);
            disc(pose, collector, s - 3, y + 1.5F, y + 2.5F, top, light);
        }
        pose.popPose();
    }

    private static void disc(PoseStack pose, SubmitNodeCollector collector, float d, float y0, float y1, TextureAtlasSprite sprite, int light) {
        final float r = d / 2;
        final float step = d >= 8 ? 2 : 1;
        box(pose, collector, 8 - r, y0, 8 - r + step, 8 + r, y1, 8 + r - step, sprite, light);
        box(pose, collector, 8 - r + step, y0, 8 - r, 8 + r - step, y1, 8 + r, sprite, light);
        if (d >= 8) box(pose, collector, 8 - r + 1, y0, 8 - r + 1, 8 + r - 1, y1, 8 + r - 1, sprite, light);
    }

    private static void leaf(PoseStack pose, SubmitNodeCollector collector, float d, float y0, float y1, TextureAtlasSprite sprite, int light, long seed) {
        final RandomSource random = RandomSource.create(seed);
        final float r = d / 2;
        box(pose, collector, 8 - r, y0, 8 - r + 2, 8 + r - 1, y1, 8 + r - 1, sprite, light);
        box(pose, collector, 8 - r + 1.5F, y0, 8 - r, 8 + r, y1, 8 + r - 2.5F, sprite, light);
        for (int i = 0; i < 4; i++) {
            final float a = i * 1.5708F + random.nextFloat();
            final float cx = 8 + (float) Math.cos(a) * (r - 0.5F), cz = 8 + (float) Math.sin(a) * (r - 0.5F);
            box(pose, collector, cx - 1, y0, cz - 1, cx + 1, y1, cz + 1, sprite, light);
        }
    }

    private static void bits(PoseStack pose, SubmitNodeCollector collector, int count, float y, TextureAtlasSprite sprite, int light, long seed) {
        final RandomSource random = RandomSource.create(seed);
        for (int i = 0; i < count; i++) {
            final float a = random.nextFloat() * 6.2832F, d = 1 + random.nextFloat() * 3.2F;
            final float cx = 8 + (float) Math.cos(a) * d, cz = 8 + (float) Math.sin(a) * d;
            final float s = 0.8F + random.nextFloat() * 0.4F;
            box(pose, collector, cx - s, y - 0.1F, cz - s, cx + s, y + 0.9F, cz + s, sprite, light);
        }
    }

    /** A box in pixels with the texture mapped by position, so overlapping boxes line up. */
    private static void box(PoseStack pose, SubmitNodeCollector collector, float x0, float y0, float z0, float x1, float y1, float z1,
                            TextureAtlasSprite sprite, int light) {
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), (p, b) ->
                cube(p, b, x0, y0, z0, x1, y1, z1, sprite, light));
    }

    private static void vertex(PoseStack.Pose p, VertexConsumer buffer, float x, float y, float z, float u, float v, int colour, int light,
                               float nx, float ny, float nz) {
        buffer.addVertex(p, x / 16, y / 16, z / 16).setColor(colour).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, nx, ny, nz);
    }

    private static void cube(PoseStack.Pose p, VertexConsumer b, float x0, float y0, float z0, float x1, float y1, float z1,
                             TextureAtlasSprite sprite, int light) {
        final float tu0 = sprite.getU(x0 / 16), tu1 = sprite.getU(x1 / 16), tv0 = sprite.getV(z0 / 16), tv1 = sprite.getV(z1 / 16);
        final float h = Math.max(1, y1 - y0) / 16;
        final float sv0 = sprite.getV(0), sv1 = sprite.getV(h);
        final float zu0 = sprite.getU(z0 / 16), zu1 = sprite.getU(z1 / 16);
        final int top = -1, side = 0xFFCCCCCC, low = 0xFF999999;
        vertex(p, b, x0, y1, z0, tu0, tv0, top, light, 0, 1, 0);
        vertex(p, b, x0, y1, z1, tu0, tv1, top, light, 0, 1, 0);
        vertex(p, b, x1, y1, z1, tu1, tv1, top, light, 0, 1, 0);
        vertex(p, b, x1, y1, z0, tu1, tv0, top, light, 0, 1, 0);
        vertex(p, b, x0, y0, z0, tu0, tv0, low, light, 0, -1, 0);
        vertex(p, b, x1, y0, z0, tu1, tv0, low, light, 0, -1, 0);
        vertex(p, b, x1, y0, z1, tu1, tv1, low, light, 0, -1, 0);
        vertex(p, b, x0, y0, z1, tu0, tv1, low, light, 0, -1, 0);
        vertex(p, b, x0, y1, z0, tu0, sv0, side, light, 0, 0, -1);
        vertex(p, b, x1, y1, z0, tu1, sv0, side, light, 0, 0, -1);
        vertex(p, b, x1, y0, z0, tu1, sv1, side, light, 0, 0, -1);
        vertex(p, b, x0, y0, z0, tu0, sv1, side, light, 0, 0, -1);
        vertex(p, b, x1, y1, z1, tu1, sv0, side, light, 0, 0, 1);
        vertex(p, b, x0, y1, z1, tu0, sv0, side, light, 0, 0, 1);
        vertex(p, b, x0, y0, z1, tu0, sv1, side, light, 0, 0, 1);
        vertex(p, b, x1, y0, z1, tu1, sv1, side, light, 0, 0, 1);
        vertex(p, b, x0, y1, z1, zu1, sv0, side, light, -1, 0, 0);
        vertex(p, b, x0, y1, z0, zu0, sv0, side, light, -1, 0, 0);
        vertex(p, b, x0, y0, z0, zu0, sv1, side, light, -1, 0, 0);
        vertex(p, b, x0, y0, z1, zu1, sv1, side, light, -1, 0, 0);
        vertex(p, b, x1, y1, z0, zu0, sv0, side, light, 1, 0, 0);
        vertex(p, b, x1, y1, z1, zu1, sv0, side, light, 1, 0, 0);
        vertex(p, b, x1, y0, z1, zu1, sv1, side, light, 1, 0, 0);
        vertex(p, b, x1, y0, z0, zu0, sv1, side, light, 1, 0, 0);
    }
}
