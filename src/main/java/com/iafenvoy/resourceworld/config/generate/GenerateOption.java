package com.iafenvoy.resourceworld.config.generate;

import com.iafenvoy.resourceworld.util.RLUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
//? >=1.20.5 {
import com.mojang.serialization.MapCodec;
//?}
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.function.Function;

public interface GenerateOption {
    Registry</*? >=1.20.5 {*/MapCodec/*?} else {*//*Codec*//*?}*/<? extends GenerateOption>> REGISTRY = new MappedRegistry<>(ResourceKey.createRegistryKey(RLUtil.id("generate_option_type")), Lifecycle.stable());

    Codec<GenerateOption> CODEC = REGISTRY.byNameCodec().dispatch("type", GenerateOption::codec, Function.identity());

    /*? >=1.20.5 {*/MapCodec/*?} else {*//*Codec*//*?}*/<? extends GenerateOption> codec();

    LevelStem createStem(RegistryAccess registries);
}
