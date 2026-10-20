package com.workshopcraft.morefood.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jspecify.annotations.Nullable;

public class CookingPotRenderState extends BlockEntityRenderState {
    public float time;
    public final StoveRenderState.RingState pot = new StoveRenderState.RingState();
    public @Nullable TextureAtlasSprite stew;
    public @Nullable TextureAtlasSprite waterSprite;
}
