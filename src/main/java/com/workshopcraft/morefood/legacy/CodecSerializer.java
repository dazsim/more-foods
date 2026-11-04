package com.workshopcraft.morefood.legacy;

//? if <26.1 {
/*import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
//? if <1.21 {
/^import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
^///?} else {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
//?}

/^* A recipe serializer driven by the recipe's map codec. ^/
public final class CodecSerializer<T extends Recipe<?>> implements RecipeSerializer<T> {
    private final MapCodec<T> codec;
    //? if >=1.21 {
    private final StreamCodec<RegistryFriendlyByteBuf, T> stream;
    //?}

    public CodecSerializer(MapCodec<T> codec) {
        this.codec = codec;
        //? if >=1.21 {
        this.stream = ByteBufCodecs.fromCodecWithRegistries(codec.codec());
        //?}
    }

    //? if <1.21 {
    /^@Override
    public T fromJson(Identifier id, JsonObject json) {
        final T recipe = codec.codec().parse(JsonOps.INSTANCE, json).getOrThrow(false, e -> {});
        if (recipe instanceof LegacyRecipe<?> legacy) legacy.setId(id);
        return recipe;
    }

    @Override
    public T fromNetwork(Identifier id, FriendlyByteBuf buf) {
        return fromJson(id, JsonParser.parseString(buf.readUtf(262144)).getAsJsonObject());
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, T recipe) {
        final JsonElement json = codec.codec().encodeStart(JsonOps.INSTANCE, recipe).getOrThrow(false, e -> {});
        buf.writeUtf(json.toString(), 262144);
    }
    ^///?} else {
    @Override
    public MapCodec<T> codec() {
        return codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return stream;
    }
    //?}
}
*///?}
