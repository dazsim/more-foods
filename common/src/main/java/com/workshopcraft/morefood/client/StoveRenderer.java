package com.workshopcraft.morefood.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.workshopcraft.morefood.MoreFood;
import com.workshopcraft.morefood.stove.HobTool;
import com.workshopcraft.morefood.stove.Pan;
import com.workshopcraft.morefood.stove.Pot;
import com.workshopcraft.morefood.stove.StoveBlock;
import com.workshopcraft.morefood.stove.StoveBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoveRenderer implements BlockEntityRenderer<StoveBlockEntity, StoveRenderState> {
    //ring centres in pixels for a stove facing north, left ring first
    private static final float[] RING_X = {11.5F, 4F};
    private static final float RING_Z = 8.5F;
    private static final int MAX_BITS = 10;
    private static final SpriteId STEW = new SpriteId(TextureAtlas.LOCATION_BLOCKS, MoreFood.id("block/stew"));
    private static final SpriteId WATER = new SpriteId(TextureAtlas.LOCATION_BLOCKS, MoreFood.id("block/pot_water"));
    private static final Map<String, String> BITS = new HashMap<>();

    static {
        bits("carrot", "minecraft:carrot");
        bits("potato", "minecraft:potato", "morefood:item_potato_peeled", "morefood:item_potato_boiled", "morefood:item_potato_mashed");
        bits("onion", "morefood:item_onion", "morefood:item_onion_chopped", "morefood:item_garlic", "morefood:item_garlic_chopped");
        bits("leek", "morefood:item_leek", "morefood:item_leek_sliced");
        bits("pea", "morefood:item_peas");
        bits("bean", "morefood:item_haricot_beans");
        bits("cabbage", "morefood:item_cabbage", "morefood:item_cabbage_shredded");
        bits("kale", "morefood:item_kale", "morefood:item_spinach", "morefood:item_lettuce");
        bits("mushroom", "minecraft:red_mushroom", "minecraft:brown_mushroom", "morefood:item_mushroom_fried");
        bits("meat", "morefood:item_minced_meat_cooked", "morefood:item_minced_lamb_cooked", "minecraft:cooked_chicken", "minecraft:cooked_beef");
        bits("bacon", "morefood:item_bacon");
        bits("sausage", "morefood:item_sausage");
        bits("tomato", "morefood:item_fruit_tomato", "morefood:item_tomato_chopped", "morefood:item_sauce_tomato");
        bits("root", "morefood:item_parsnip", "morefood:item_turnip", "morefood:item_swede");
    }

    private static void bits(String texture, String... items) {
        for (String item : items) BITS.put(item, texture);
    }

    private final ItemModelResolver itemModelResolver;
    private final SpriteGetter sprites;

    public StoveRenderer(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
        sprites = context.sprites();
    }

    @Override
    public StoveRenderState createRenderState() {
        return new StoveRenderState();
    }

    @Override
    public void extractRenderState(StoveBlockEntity stove, StoveRenderState state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(stove, state, partialTicks, cameraPosition, breakProgress);
        final Level level = stove.getLevel();
        if (level != null) state.lightCoords = LevelRenderer.getLightCoords(level, stove.getBlockPos().above());
        state.facing = stove.getBlockState().getValue(StoveBlock.FACING);
        state.time = (level != null ? level.getGameTime() % 24000 : 0) + partialTicks;
        state.stew = sprites.get(STEW);
        state.waterSprite = sprites.get(WATER);
        final int seed = (int) stove.getBlockPos().asLong();
        for (int r = 0; r < 2; r++) {
            final StoveBlockEntity.Ring ring = stove.ring(r);
            final StoveRenderState.RingState out = state.rings[r];
            out.tool = ring.tool;
            out.pan.clear();
            out.bits.clear();
            out.liquid = false;
            if (ring.tool == HobTool.PAN) {
                for (int i = 0; i < Pan.SPOTS; i++) {
                    final ItemStackRenderState item = new ItemStackRenderState();
                    itemModelResolver.updateForTopItem(item, ring.pan.get(i), ItemDisplayContext.FIXED, level, null, seed + r * 8 + i);
                    out.pan.add(item);
                }
            } else if (ring.tool == HobTool.POT) {
                final Pot pot = ring.pot;
                out.liquid = pot.water() > 0 || pot.portions() > 0 || pot.isCooking();
                out.water = pot.colour() == Pot.WATER_COLOUR;
                out.colour = pot.colour();
                out.surface = out.liquid ? pot.surface() : 17.2F;
                final List<Identifier> ids = pot.bits();
                final RandomSource random = RandomSource.create(seed * 31L + r);
                for (int i = 0; i < ids.size() && out.bits.size() < MAX_BITS; i++) {
                    final int cubes = 1 + random.nextInt(2);
                    for (int c = 0; c < cubes && out.bits.size() < MAX_BITS; c++) {
                        final StoveRenderState.Bit bit = bit(ids.get(i), random);
                        if (bit != null) out.bits.add(bit);
                    }
                }
            }
        }
    }

    private StoveRenderState.@Nullable Bit bit(Identifier id, RandomSource random) {
        final String texture = BITS.get(id.toString());
        if (texture == null) return null;
        final StoveRenderState.Bit bit = new StoveRenderState.Bit();
        bit.sprite = sprites.get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, MoreFood.id("block/bits/" + texture)));
        bit.size = 1.2F + random.nextFloat() * 0.8F;
        bit.radius = random.nextFloat() * (2.4F - bit.size / 2);
        bit.angle = random.nextFloat() * Mth.TWO_PI;
        bit.phase = random.nextFloat() * Mth.TWO_PI;
        return bit;
    }

    @Override
    public void submit(StoveRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.translate(0.5F, 0, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot() + 180));
        pose.translate(-0.5F, 0, -0.5F);
        for (int r = 0; r < 2; r++) {
            final StoveRenderState.RingState ring = state.rings[r];
            if (ring.tool == HobTool.PAN) submitPan(state, ring, RING_X[r], pose, collector);
            else if (ring.tool == HobTool.POT) submitPot(state, ring, RING_X[r], pose, collector);
        }
        pose.popPose();
    }

    private static void submitPan(StoveRenderState state, StoveRenderState.RingState ring, float cx, PoseStack pose, SubmitNodeCollector collector) {
        for (int i = 0; i < ring.pan.size(); i++) {
            final ItemStackRenderState item = ring.pan.get(i);
            if (item.isEmpty()) continue;
            pose.pushPose();
            pose.translate((cx + (i % 2 == 0 ? -1.25F : 1.25F)) / 16, 16.75F / 16, (RING_Z + (i < 2 ? -1.25F : 1.25F)) / 16);
            pose.mulPose(Axis.YP.rotationDegrees(i * 90 + 20));
            pose.mulPose(Axis.XP.rotationDegrees(90));
            pose.scale(0.22F, 0.22F, 0.22F);
            item.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }

    private static void submitPot(StoveRenderState state, StoveRenderState.RingState ring, float cx, PoseStack pose, SubmitNodeCollector collector) {
        final float x0 = (cx - 2.5F) / 16, x1 = (cx + 2.5F) / 16, z0 = (RING_Z - 3) / 16, z1 = (RING_Z + 3) / 16;
        final float y = ring.surface / 16;
        final int light = state.lightCoords;
        if (ring.liquid) {
            final TextureAtlasSprite sprite = ring.water ? state.waterSprite : state.stew;
            if (sprite != null) {
                final int colour = ring.water ? 0xE0FFFFFF : ARGB.opaque(ring.colour);
                collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (p, buffer) ->
                        quad(p, buffer, x0, z0, x1, z1, y, sprite, 0.1F, 0.9F, colour, light));
            }
        }
        for (StoveRenderState.Bit bit : ring.bits) {
            final float a = bit.angle + (ring.liquid ? state.time * 0.015F : 0);
            final float bx = cx + Mth.cos(a) * bit.radius;
            final float bz = RING_Z + Mth.sin(a) * bit.radius * 1.2F;
            final float bob = ring.liquid ? 0.5F * Mth.sin(state.time * 0.08F + bit.phase) : 0;
            final float by = (ring.liquid ? ring.surface - bit.size * 0.65F : 17) + bob;
            final float s = bit.size / 2;
            final RenderType type = RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS);
            collector.submitCustomGeometry(pose, type, (p, buffer) ->
                    cube(p, buffer, (bx - s) / 16, by / 16, (bz - s) / 16, (bx + s) / 16, (by + bit.size) / 16, (bz + s) / 16, bit.sprite, light));
        }
    }

    private static void vertex(PoseStack.Pose p, VertexConsumer buffer, float x, float y, float z, float u, float v, int colour, int light,
                               float nx, float ny, float nz) {
        buffer.addVertex(p, x, y, z).setColor(colour).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, nx, ny, nz);
    }

    private static void quad(PoseStack.Pose p, VertexConsumer buffer, float x0, float z0, float x1, float z1, float y,
                             TextureAtlasSprite sprite, float from, float to, int colour, int light) {
        final float u0 = sprite.getU(from), u1 = sprite.getU(to), v0 = sprite.getV(from), v1 = sprite.getV(to);
        vertex(p, buffer, x0, y, z0, u0, v0, colour, light, 0, 1, 0);
        vertex(p, buffer, x0, y, z1, u0, v1, colour, light, 0, 1, 0);
        vertex(p, buffer, x1, y, z1, u1, v1, colour, light, 0, 1, 0);
        vertex(p, buffer, x1, y, z0, u1, v0, colour, light, 0, 1, 0);
    }

    private static void cube(PoseStack.Pose p, VertexConsumer b, float x0, float y0, float z0, float x1, float y1, float z1,
                             TextureAtlasSprite sprite, int light) {
        final float u0 = sprite.getU(0.375F), u1 = sprite.getU(0.5F), v0 = sprite.getV(0.375F), v1 = sprite.getV(0.5F);
        final int top = -1, side = 0xFFCCCCCC, low = 0xFF999999;
        vertex(p, b, x0, y1, z0, u0, v0, top, light, 0, 1, 0);
        vertex(p, b, x0, y1, z1, u0, v1, top, light, 0, 1, 0);
        vertex(p, b, x1, y1, z1, u1, v1, top, light, 0, 1, 0);
        vertex(p, b, x1, y1, z0, u1, v0, top, light, 0, 1, 0);
        vertex(p, b, x0, y0, z0, u0, v0, low, light, 0, -1, 0);
        vertex(p, b, x1, y0, z0, u1, v0, low, light, 0, -1, 0);
        vertex(p, b, x1, y0, z1, u1, v1, low, light, 0, -1, 0);
        vertex(p, b, x0, y0, z1, u0, v1, low, light, 0, -1, 0);
        vertex(p, b, x0, y1, z0, u0, v0, side, light, 0, 0, -1);
        vertex(p, b, x1, y1, z0, u1, v0, side, light, 0, 0, -1);
        vertex(p, b, x1, y0, z0, u1, v1, side, light, 0, 0, -1);
        vertex(p, b, x0, y0, z0, u0, v1, side, light, 0, 0, -1);
        vertex(p, b, x1, y1, z1, u0, v0, side, light, 0, 0, 1);
        vertex(p, b, x0, y1, z1, u1, v0, side, light, 0, 0, 1);
        vertex(p, b, x0, y0, z1, u1, v1, side, light, 0, 0, 1);
        vertex(p, b, x1, y0, z1, u0, v1, side, light, 0, 0, 1);
        vertex(p, b, x0, y1, z1, u0, v0, side, light, -1, 0, 0);
        vertex(p, b, x0, y1, z0, u1, v0, side, light, -1, 0, 0);
        vertex(p, b, x0, y0, z0, u1, v1, side, light, -1, 0, 0);
        vertex(p, b, x0, y0, z1, u0, v1, side, light, -1, 0, 0);
        vertex(p, b, x1, y1, z0, u0, v0, side, light, 1, 0, 0);
        vertex(p, b, x1, y1, z1, u1, v0, side, light, 1, 0, 0);
        vertex(p, b, x1, y0, z1, u1, v1, side, light, 1, 0, 0);
        vertex(p, b, x1, y0, z0, u0, v1, side, light, 1, 0, 0);
    }
}
