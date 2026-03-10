package com.iafenvoy.resourceworld.config.generate;

import com.iafenvoy.resourceworld.util.RLUtil;
import com.mojang.serialization.Codec;
//? >=1.20.5 {
import com.mojang.serialization.MapCodec;
//?}
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;

public record MirrorGenerateOption(ResourceKey<LevelStem> target) implements GenerateOption {
    public static final /*? >=1.20.5 {*/MapCodec/*?} else {*//*Codec*//*?}*/<MirrorGenerateOption> CODEC = RecordCodecBuilder./*? >=1.20.5 {*/mapCodec/*?} else {*//*create*//*?}*/(i -> i.group(
            ResourceKey.codec(Registries.LEVEL_STEM).fieldOf("target").forGetter(MirrorGenerateOption::target)
    ).apply(i, MirrorGenerateOption::new));

    @Override
    public /*? >=1.20.5 {*/MapCodec/*?} else {*//*Codec*//*?}*/<MirrorGenerateOption> codec() {
        return CODEC;
    }

    @Override
    public LevelStem createStem(RegistryAccess registries) {
        return registries.registryOrThrow(Registries.LEVEL_STEM).getOrThrow(this.target);
    }

    static {
        Registry.register(REGISTRY, RLUtil.id("mirror"), CODEC);
    }
}
