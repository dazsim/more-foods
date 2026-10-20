package com.workshopcraft.morefood.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

/** How each item looks as a layer on the assembly board, read from assets/NAMESPACE/board_layers/ITEM.json. */
public class BoardLayers extends SimpleJsonResourceReloadListener<BoardLayers.Spec> {
    public enum Shape implements StringRepresentable {
        DISC, SQUARE, LEAF, BITS, BUN;

        public static final Codec<Shape> CODEC = StringRepresentable.fromEnum(Shape::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    public record Spec(Shape shape, float size, float thickness, Identifier texture, Optional<Identifier> top, int count) {
        public static final Codec<Spec> CODEC = RecordCodecBuilder.create(i -> i.group(
                Shape.CODEC.fieldOf("shape").forGetter(Spec::shape),
                Codec.floatRange(1, 14).optionalFieldOf("size", 12F).forGetter(Spec::size),
                Codec.floatRange(0, 4).optionalFieldOf("thickness", 1F).forGetter(Spec::thickness),
                Identifier.CODEC.fieldOf("texture").forGetter(Spec::texture),
                Identifier.CODEC.optionalFieldOf("top").forGetter(Spec::top),
                ExtraCodecs.intRange(1, 10).optionalFieldOf("count", 6).forGetter(Spec::count)
        ).apply(i, Spec::new));
    }

    public static final Identifier ID = Identifier.fromNamespaceAndPath("morefood", "board_layers");
    private static Map<Identifier, Spec> specs = Map.of();

    public BoardLayers() {
        super(Spec.CODEC, FileToIdConverter.json("board_layers"));
    }

    public static @Nullable Spec get(Identifier item) {
        return specs.get(item);
    }

    @Override
    protected void apply(Map<Identifier, Spec> loaded, ResourceManager manager, ProfilerFiller profiler) {
        specs = Map.copyOf(loaded);
    }
}
