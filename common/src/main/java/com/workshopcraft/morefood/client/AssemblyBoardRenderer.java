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

import java.util.ArrayList;
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
                layer.side = layer.spec.side().map(id -> sprites.get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, id))).orElse(null);
            } else {
                itemModelResolver.updateForTopItem(layer.item, stack, ItemDisplayContext.FIXED, board.getLevel(), null, (int) layer.seed);
            }
            state.layers.add(layer);
        }
    }

    /** The outline the base layer leaves for the layers above it: round or square, w along x, d along z. */
    private record Foot(boolean round, float w, float d) {}

    private static final float TIN = 12, TIN_IN = 9, TIN_H = 3;

    @Override
    public void submit(AssemblyBoardRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.layers.isEmpty()) return;
        pose.pushPose();
        pose.translate(0.5F, 0, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot() + 180));
        pose.translate(-0.5F, 0, -0.5F);
        final int light = state.lightCoords;
        final List<AssemblyBoardRenderState.Layer> layers = state.layers;
        final AssemblyBoardRenderState.Layer base = layers.getFirst();
        if (state.complete && base.spec != null && base.sprite != null) {
            if (base.spec.shape() == BoardLayers.Shape.WRAP) {
                wrap(pose, collector, base.sprite, layers, light);
                pose.popPose();
                return;
            }
            if (base.spec.shape() == BoardLayers.Shape.PASTRY && !has(layers, BoardLayers.Shape.FILL)) {
                pasty(pose, collector, base.sprite, base.sprite, light);
                pose.popPose();
                return;
            }
        }
        Foot foot = new Foot(true, 12, 12);
        float y = BOARD_TOP, above = 0, tinTop = -1;
        for (int i = 0; i < layers.size(); i++) {
            final AssemblyBoardRenderState.Layer layer = layers.get(i);
            final BoardLayers.Spec spec = layer.spec;
            if (spec == null || layer.sprite == null) {
                y += above;
                above = 0;
                pose.pushPose();
                pose.translate(0.5F, (y + 0.3F) / 16, 0.5F);
                pose.mulPose(Axis.XP.rotationDegrees(90));
                pose.scale(0.6F, 0.6F, 0.6F);
                layer.item.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, 0);
                pose.popPose();
                y += 1;
                continue;
            }
            final TextureAtlasSprite sprite = layer.sprite;
            final TextureAtlasSprite side = layer.side != null ? layer.side : sprite;
            final float t = spec.thickness(), s = spec.size();
            if (spec.shape() == BoardLayers.Shape.BITS) {
                bits(pose, collector, spec.count(), y, foot, sprite, light, layer.seed);
                above = Math.max(above, 0.6F);
                continue;
            }
            y += above;
            above = 0;
            switch (spec.shape()) {
                case DISC, WRAP -> {
                    disc(pose, collector, s, y, y + t, sprite, side, light);
                    if (i == 0) foot = new Foot(true, s, s);
                    y += t;
                }
                case SQUARE -> {
                    box(pose, collector, 8 - s / 2, y, 8 - s / 2, 8 + s / 2, y + t, 8 + s / 2, sprite, side, light);
                    if (i == 0) foot = new Foot(false, s, s);
                    y += t;
                }
                case LEAF -> {
                    leaf(pose, collector, s, y, y + t, sprite, light, layer.seed);
                    y += t;
                }
                case BUN -> {
                    if (i == 0) {
                        disc(pose, collector, s, y, y + t, sprite, side, light);
                        foot = new Foot(true, s, s);
                        y += t;
                    } else {
                        final TextureAtlasSprite crown = layer.top != null ? layer.top : sprite;
                        final TextureAtlasSprite crust = layer.side != null ? layer.side : crown;
                        disc(pose, collector, s - 1, y, y + 1.5F, crown, crust, light);
                        disc(pose, collector, s - 4, y + 1.5F, y + 2.5F, crown, crust, light);
                        y += 2.5F;
                    }
                }
                case SHEET -> {
                    sheet(pose, collector, foot, spec.inset(), y, y + t, sprite, side, light);
                    y += t;
                }
                case MASH -> {
                    final float y0 = tinTop > 0 ? Math.max(y, tinTop - 0.5F) : y;
                    sheet(pose, collector, foot, spec.inset(), y0, y0 + t, sprite, side, light);
                    lumps(pose, collector, foot, spec.inset(), y0 + t, sprite, light, layer.seed);
                    y = y0 + t;
                }
                case LOAF -> {
                    loaf(pose, collector, s, y, y + t, sprite, side, light);
                    if (i == 0) foot = new Foot(false, s - 1, 4);
                    y += t;
                }
                case PASTRY -> {
                    final AssemblyBoardRenderState.Layer next = i + 1 < layers.size() ? layers.get(i + 1) : null;
                    if (i == 0 && next != null && next.spec != null && next.spec.shape() == BoardLayers.Shape.FILL) {
                        tin(pose, collector, y, sprite, side, light);
                        disc(pose, collector, TIN_IN, y + 0.5F, y + 1, sprite, light);
                        tinTop = y + TIN_H;
                        foot = new Foot(true, TIN_IN, TIN_IN);
                        y += 1;
                    } else if (i == 0) {
                        disc(pose, collector, TIN, y, y + 0.5F, sprite, light);
                        y += 0.5F;
                    } else {
                        final float y0 = Math.max(y, tinTop);
                        lid(pose, collector, y0, layer.top != null ? layer.top : sprite, sprite, light);
                        y = y0 + 1;
                    }
                }
                case FILL -> {
                    if (i == 0) {
                        tin(pose, collector, y, side, side, light);
                        tinTop = y + TIN_H;
                        foot = new Foot(true, TIN_IN, TIN_IN);
                        y += 0.5F;
                    }
                    final float top = tinTop > 0 ? tinTop - 0.5F : y + Math.max(t, 0.5F);
                    disc(pose, collector, TIN_IN, y, top, sprite, light);
                    y = top;
                }
                default -> {
                }
            }
            if (y > MAX_TOP) break;
        }
        pose.popPose();
    }

    private static boolean has(List<AssemblyBoardRenderState.Layer> layers, BoardLayers.Shape shape) {
        for (AssemblyBoardRenderState.Layer layer : layers) {
            if (layer.spec != null && layer.spec.shape() == shape) return true;
        }
        return false;
    }

    private static void disc(PoseStack pose, SubmitNodeCollector collector, float d, float y0, float y1, TextureAtlasSprite sprite, int light) {
        disc(pose, collector, d, y0, y1, sprite, sprite, light);
    }

    private static void disc(PoseStack pose, SubmitNodeCollector collector, float d, float y0, float y1, TextureAtlasSprite sprite,
                             TextureAtlasSprite side, int light) {
        final float r = d / 2;
        final float step = d >= 8 ? 2 : 1;
        box(pose, collector, 8 - r, y0, 8 - r + step, 8 + r, y1, 8 + r - step, sprite, side, light);
        box(pose, collector, 8 - r + step, y0, 8 - r, 8 + r - step, y1, 8 + r, sprite, side, light);
        if (d >= 8) box(pose, collector, 8 - r + 1, y0, 8 - r + 1, 8 + r - 1, y1, 8 + r - 1, sprite, side, light);
    }

    /** A thin layer that follows the outline of the base, a little inside it. */
    private static void sheet(PoseStack pose, SubmitNodeCollector collector, Foot foot, float inset, float y0, float y1, TextureAtlasSprite sprite,
                              TextureAtlasSprite side, int light) {
        if (foot.round() && foot.w() >= 12) {
            disc(pose, collector, foot.w() - 2 * inset, y0, y1, sprite, side, light);
            return;
        }
        if (foot.round()) {
            //a slice on a bun stays square, its corners hanging over the edge
            final float h = (foot.w() - 2 * inset) / 2 + 0.5F;
            box(pose, collector, 8 - h, y0, 8 - h, 8 + h, y1, 8 + h, sprite, side, light);
            return;
        }
        final float hw = foot.w() / 2 - inset, hd = foot.d() / 2 - inset;
        box(pose, collector, 8 - hw, y0, 8 - hd, 8 + hw, y1, 8 + hd, sprite, side, light);
    }

    private static void lumps(PoseStack pose, SubmitNodeCollector collector, Foot foot, float inset, float y, TextureAtlasSprite sprite, int light, long seed) {
        final RandomSource random = RandomSource.create(seed);
        final float rx = Math.max(0.5F, foot.w() / 2 - inset - 2), rz = Math.max(0.5F, foot.d() / 2 - inset - 2);
        for (int i = 0; i < 6; i++) {
            final float a = random.nextFloat() * 6.2832F, k = 0.2F + random.nextFloat() * 0.8F;
            final float cx = 8 + (float) Math.cos(a) * rx * k, cz = 8 + (float) Math.sin(a) * rz * k;
            final float s = 0.75F + random.nextFloat() * 0.5F, h = 0.4F + random.nextFloat() * 0.5F;
            box(pose, collector, cx - s, y - 0.1F, cz - s, cx + s, y + h, cz + s, sprite, light);
        }
    }

    /** Half a baguette, cut side up, running left to right. */
    private static void loaf(PoseStack pose, SubmitNodeCollector collector, float length, float y0, float y1, TextureAtlasSprite cut,
                             TextureAtlasSprite crust, int light) {
        final float h = length / 2;
        box(pose, collector, 8 - h, y0, 6, 8 + h, y1, 10, cut, crust, light);
        box(pose, collector, 8 - h + 1, y0, 5.5F, 8 + h - 1, y1 - 0.5F, 10.5F, crust, crust, light);
        box(pose, collector, 8 - h - 0.5F, y0, 6.5F, 8 - h, y1 - 0.5F, 9.5F, crust, crust, light);
        box(pose, collector, 8 + h, y0, 6.5F, 8 + h + 0.5F, y1 - 0.5F, 9.5F, crust, crust, light);
    }

    /** A round pie tin, 12 across and 3 deep, with its rim in the rim texture. */
    private static void tin(PoseStack pose, SubmitNodeCollector collector, float y0, TextureAtlasSprite rim, TextureAtlasSprite metal, int light) {
        final float y1 = y0 + TIN_H, w = 1.5F;
        box(pose, collector, 3, y0, 3, 13, y0 + 0.5F, 13, metal, light);
        box(pose, collector, 4, y0, 2, 12, y1, 2 + w, rim, metal, light);
        box(pose, collector, 4, y0, 14 - w, 12, y1, 14, rim, metal, light);
        box(pose, collector, 2, y0, 4, 2 + w, y1, 12, rim, metal, light);
        box(pose, collector, 14 - w, y0, 4, 14, y1, 12, rim, metal, light);
        box(pose, collector, 3, y0, 3, 4.5F, y1, 4.5F, rim, metal, light);
        box(pose, collector, 11.5F, y0, 3, 13, y1, 4.5F, rim, metal, light);
        box(pose, collector, 3, y0, 11.5F, 4.5F, y1, 13, rim, metal, light);
        box(pose, collector, 11.5F, y0, 11.5F, 13, y1, 13, rim, metal, light);
    }

    /** A pastry lid with a crimped edge. */
    private static void lid(PoseStack pose, SubmitNodeCollector collector, float y0, TextureAtlasSprite top, TextureAtlasSprite pastry, int light) {
        disc(pose, collector, TIN, y0, y0 + 0.75F, top, pastry, light);
        for (int i = 0; i < 4; i++) {
            final float a = 3.5F + i * 2.25F;
            box(pose, collector, a, y0, 1.6F, a + 1, y0 + 1, 2.4F, pastry, light);
            box(pose, collector, a, y0, 13.6F, a + 1, y0 + 1, 14.4F, pastry, light);
            box(pose, collector, 1.6F, y0, a, 2.4F, y0 + 1, a + 1, pastry, light);
            box(pose, collector, 13.6F, y0, a, 14.4F, y0 + 1, a + 1, pastry, light);
        }
    }

    /** A finished pasty: the pastry folded up over its filling, a half moon from the side with the crimp along the top. */
    private static void pasty(PoseStack pose, SubmitNodeCollector collector, TextureAtlasSprite pastry, TextureAtlasSprite crimp, int light) {
        final float y = BOARD_TOP;
        for (int i = 0; i < 12; i++) {
            final float x0 = 2 + i, t = (x0 + 0.5F - 8) / 6.5F, f = (float) Math.sqrt(Math.max(0, 1 - t * t));
            final float h = Math.max(1, 4 * f), w = Math.max(2, 6.5F * f);
            box(pose, collector, x0, y, 8 - w / 2, x0 + 1, y + h * 0.55F, 8 + w / 2, pastry, light);
            box(pose, collector, x0, y, 8 - w / 3, x0 + 1, y + h, 8 + w / 3, pastry, light);
            final float c = i % 2 == 0 ? -0.35F : 0.35F;
            if (i > 0 && i < 11) box(pose, collector, x0, y + h - 0.2F, 7.6F + c, x0 + 1, y + h + 0.7F, 8.4F + c, crimp, light);
        }
    }

    /** A finished wrap, rolled up with the filling showing at both ends. */
    private static void wrap(PoseStack pose, SubmitNodeCollector collector, TextureAtlasSprite bread, List<AssemblyBoardRenderState.Layer> layers, int light) {
        final float y = BOARD_TOP;
        box(pose, collector, 2.5F, y, 6.5F, 13.5F, y + 5, 9.5F, bread, light);
        box(pose, collector, 2.5F, y + 1, 5.5F, 13.5F, y + 4, 10.5F, bread, light);
        final List<TextureAtlasSprite> fill = new ArrayList<>();
        for (int i = 1; i < layers.size(); i++) {
            if (layers.get(i).sprite != null) fill.add(layers.get(i).sprite);
        }
        if (fill.isEmpty()) return;
        final TextureAtlasSprite a = fill.getFirst(), b = fill.size() > 1 ? fill.get(1) : a, c = fill.getLast();
        for (float x : new float[]{2.25F, 13.5F}) {
            box(pose, collector, x, y + 1, 6.5F, x + 0.25F, y + 4, 9.5F, a, light);
            box(pose, collector, x - 0.05F, y + 2.25F, 6.75F, x + 0.3F, y + 3.5F, 8, b, light);
            box(pose, collector, x - 0.05F, y + 1.25F, 8, x + 0.3F, y + 2.5F, 9.25F, c, light);
        }
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

    /** Small cubes scattered over the base, the same way the board's first pizzas had them. */
    private static void bits(PoseStack pose, SubmitNodeCollector collector, int count, float y, Foot foot, TextureAtlasSprite sprite, int light, long seed) {
        final RandomSource random = RandomSource.create(seed);
        final float rx = Math.max(0.5F, foot.w() / 2 - 1.8F), rz = Math.max(0.5F, foot.d() / 2 - 1.8F);
        for (int i = 0; i < count; i++) {
            final float a = random.nextFloat() * 6.2832F, k = (1 + random.nextFloat() * 3.2F) / 4.2F;
            final float cx = 8 + (float) Math.cos(a) * rx * k, cz = 8 + (float) Math.sin(a) * rz * k;
            final float s = 0.8F + random.nextFloat() * 0.4F;
            box(pose, collector, cx - s, y - 0.1F, cz - s, cx + s, y + 0.9F, cz + s, sprite, light);
        }
    }

    private static void box(PoseStack pose, SubmitNodeCollector collector, float x0, float y0, float z0, float x1, float y1, float z1,
                            TextureAtlasSprite sprite, int light) {
        box(pose, collector, x0, y0, z0, x1, y1, z1, sprite, sprite, light);
    }

    /** A box in pixels with the texture mapped by position, so overlapping boxes line up. */
    private static void box(PoseStack pose, SubmitNodeCollector collector, float x0, float y0, float z0, float x1, float y1, float z1,
                            TextureAtlasSprite top, TextureAtlasSprite side, int light) {
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), (p, b) ->
                cube(p, b, x0, y0, z0, x1, y1, z1, top, side, light));
    }

    private static void vertex(PoseStack.Pose p, VertexConsumer buffer, float x, float y, float z, float u, float v, int colour, int light,
                               float nx, float ny, float nz) {
        buffer.addVertex(p, x / 16, y / 16, z / 16).setColor(colour).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, nx, ny, nz);
    }

    private static void cube(PoseStack.Pose p, VertexConsumer b, float x0, float y0, float z0, float x1, float y1, float z1,
                             TextureAtlasSprite sprite, TextureAtlasSprite side, int light) {
        final float tu0 = sprite.getU(x0 / 16), tu1 = sprite.getU(x1 / 16), tv0 = sprite.getV(z0 / 16), tv1 = sprite.getV(z1 / 16);
        final float h = Math.max(1, y1 - y0) / 16;
        final float sv0 = side.getV(0), sv1 = side.getV(h);
        final float su0 = side.getU(x0 / 16), su1 = side.getU(x1 / 16);
        final float zu0 = side.getU(z0 / 16), zu1 = side.getU(z1 / 16);
        final int top = -1, lit = 0xFFCCCCCC, low = 0xFF999999;
        vertex(p, b, x0, y1, z0, tu0, tv0, top, light, 0, 1, 0);
        vertex(p, b, x0, y1, z1, tu0, tv1, top, light, 0, 1, 0);
        vertex(p, b, x1, y1, z1, tu1, tv1, top, light, 0, 1, 0);
        vertex(p, b, x1, y1, z0, tu1, tv0, top, light, 0, 1, 0);
        vertex(p, b, x0, y0, z0, tu0, tv0, low, light, 0, -1, 0);
        vertex(p, b, x1, y0, z0, tu1, tv0, low, light, 0, -1, 0);
        vertex(p, b, x1, y0, z1, tu1, tv1, low, light, 0, -1, 0);
        vertex(p, b, x0, y0, z1, tu0, tv1, low, light, 0, -1, 0);
        vertex(p, b, x0, y1, z0, su0, sv0, lit, light, 0, 0, -1);
        vertex(p, b, x1, y1, z0, su1, sv0, lit, light, 0, 0, -1);
        vertex(p, b, x1, y0, z0, su1, sv1, lit, light, 0, 0, -1);
        vertex(p, b, x0, y0, z0, su0, sv1, lit, light, 0, 0, -1);
        vertex(p, b, x1, y1, z1, su1, sv0, lit, light, 0, 0, 1);
        vertex(p, b, x0, y1, z1, su0, sv0, lit, light, 0, 0, 1);
        vertex(p, b, x0, y0, z1, su0, sv1, lit, light, 0, 0, 1);
        vertex(p, b, x1, y0, z1, su1, sv1, lit, light, 0, 0, 1);
        vertex(p, b, x0, y1, z1, zu1, sv0, lit, light, -1, 0, 0);
        vertex(p, b, x0, y1, z0, zu0, sv0, lit, light, -1, 0, 0);
        vertex(p, b, x0, y0, z0, zu0, sv1, lit, light, -1, 0, 0);
        vertex(p, b, x0, y0, z1, zu1, sv1, lit, light, -1, 0, 0);
        vertex(p, b, x1, y1, z0, zu0, sv0, lit, light, 1, 0, 0);
        vertex(p, b, x1, y1, z1, zu1, sv0, lit, light, 1, 0, 0);
        vertex(p, b, x1, y0, z1, zu1, sv1, lit, light, 1, 0, 0);
        vertex(p, b, x1, y0, z0, zu0, sv1, lit, light, 1, 0, 0);
    }
}
