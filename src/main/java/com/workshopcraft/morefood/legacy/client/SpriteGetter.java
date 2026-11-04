package com.workshopcraft.morefood.legacy.client;

//? if <26.1 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/^* Stands in for the 26.1 sprite getter, looking sprites up in the loaded atlases. ^/
public final class SpriteGetter {
    private static final SpriteGetter INSTANCE = new SpriteGetter();

    public static SpriteGetter of(BlockEntityRendererProvider.Context context) {
        return INSTANCE;
    }

    public TextureAtlasSprite get(SpriteId id) {
        return Minecraft.getInstance().getTextureAtlas(id.atlasLocation()).apply(id.texture());
    }
}
*///?}
