package com.workshopcraft.morefood.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import com.workshopcraft.morefood.stove.HobTool;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StoveRenderState extends BlockEntityRenderState {
    public static class Bit {
        public TextureAtlasSprite sprite;
        public float size;
        public float radius;
        public float angle;
        public float phase;
    }

    public static class RingState {
        public HobTool tool = HobTool.NONE;
        public final List<ItemStackRenderState> pan = new ArrayList<>();
        public boolean liquid;
        public boolean water;
        public float surface;
        public int colour;
        public final List<Bit> bits = new ArrayList<>();
    }

    public Direction facing = Direction.NORTH;
    public float time;
    public final RingState[] rings = {new RingState(), new RingState()};
    public @Nullable TextureAtlasSprite stew;
    public @Nullable TextureAtlasSprite waterSprite;
}
