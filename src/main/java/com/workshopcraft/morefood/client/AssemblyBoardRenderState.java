package com.workshopcraft.morefood.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AssemblyBoardRenderState extends BlockEntityRenderState {
    public static class Layer {
        public BoardLayers.@Nullable Spec spec;
        public @Nullable TextureAtlasSprite sprite;
        public @Nullable TextureAtlasSprite top;
        public @Nullable TextureAtlasSprite side;
        public final ItemStackRenderState item = new ItemStackRenderState();
        public long seed;
    }

    public Direction facing = Direction.NORTH;
    public boolean complete;
    public final List<Layer> layers = new ArrayList<>();
}
