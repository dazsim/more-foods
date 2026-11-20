package com.workshopcraft.morefood.machine;

//? if >=1.20.5 {
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.workshopcraft.workshopcore.machine.MachineParts;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** The fitted parts stored on a dropped kitchen machine, so placing it builds the same machine again. */
public record MachinePartsData(ItemStack chassis, ItemStack core, ItemStack board, List<ItemStack> upgrades, ItemStack panels, ItemStack worktop) {
    public static final Codec<MachinePartsData> CODEC = RecordCodecBuilder.create(i -> i.group(
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("chassis", ItemStack.EMPTY).forGetter(MachinePartsData::chassis),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("core", ItemStack.EMPTY).forGetter(MachinePartsData::core),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("board", ItemStack.EMPTY).forGetter(MachinePartsData::board),
            ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("upgrades", List.of()).forGetter(MachinePartsData::upgrades),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("panels", ItemStack.EMPTY).forGetter(MachinePartsData::panels),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("worktop", ItemStack.EMPTY).forGetter(MachinePartsData::worktop)
    ).apply(i, MachinePartsData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MachinePartsData> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, MachinePartsData::chassis,
            ItemStack.OPTIONAL_STREAM_CODEC, MachinePartsData::core,
            ItemStack.OPTIONAL_STREAM_CODEC, MachinePartsData::board,
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), MachinePartsData::upgrades,
            ItemStack.OPTIONAL_STREAM_CODEC, MachinePartsData::panels,
            ItemStack.OPTIONAL_STREAM_CODEC, MachinePartsData::worktop,
            MachinePartsData::new);

    public MachinePartsData {
        upgrades = List.copyOf(upgrades);
    }

    public static MachinePartsData of(MachineParts parts) {
        return new MachinePartsData(parts.chassis().copy(), parts.core().copy(), parts.board().copy(),
                parts.upgrades().stream().map(ItemStack::copy).toList(), parts.panels().copy(), parts.worktop().copy());
    }

    public MachineParts parts() {
        return new MachineParts(chassis.copy(), core.copy(), board.copy(),
                upgrades.stream().map(ItemStack::copy).toList(), panels.copy(), worktop.copy());
    }

    public boolean isEmpty() {
        return chassis.isEmpty() && core.isEmpty();
    }

    public List<ItemStack> listed() {
        final List<ItemStack> out = new ArrayList<>();
        for (ItemStack stack : List.of(chassis, core, board)) if (!stack.isEmpty()) out.add(stack);
        for (ItemStack stack : upgrades) if (!stack.isEmpty()) out.add(stack);
        for (ItemStack stack : List.of(panels, worktop)) if (!stack.isEmpty()) out.add(stack);
        return out;
    }
}
//?}
