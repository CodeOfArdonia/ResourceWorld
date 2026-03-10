package com.iafenvoy.resourceworld.config.generate;

import com.iafenvoy.resourceworld.util.RLUtil;
import com.mojang.serialization.Codec;
//? >=1.20.5 {
import com.mojang.serialization.MapCodec;
//?}
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.screens.PresetFlatWorldScreen;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public record FlatGenerateOption(String preset) implements GenerateOption {
    public static final /*? >=1.20.5 {*/MapCodec/*?} else {*//*Codec*//*?}*/<FlatGenerateOption> CODEC = RecordCodecBuilder./*? >=1.20.5 {*/mapCodec/*?} else {*//*create*//*?}*/(i -> i.group(
            Codec.STRING.fieldOf("preset").forGetter(FlatGenerateOption::preset)
    ).apply(i, FlatGenerateOption::new));

    @Override
    public /*? >=1.20.5 {*/MapCodec/*?} else {*//*Codec*//*?}*/<? extends GenerateOption> codec() {
        return CODEC;
    }

    @Override
    public LevelStem createStem(RegistryAccess registries) {
        return new LevelStem(registries.registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD), new FlatLevelSource(createFromString(registries, this.preset)));
    }

    public static FlatLevelGeneratorSettings createFromString(RegistryAccess registries, String preset) {
        HolderGetter<Biome> biomeGetter = registries.lookupOrThrow(Registries.BIOME);
        HolderGetter<StructureSet> structureGetter = registries.lookupOrThrow(Registries.STRUCTURE_SET);
        HolderGetter<PlacedFeature> featureGetter = registries.lookupOrThrow(Registries.PLACED_FEATURE);
        HolderGetter<Block> blockGetter = registries.lookupOrThrow(Registries.BLOCK);
        return PresetFlatWorldScreen.fromString(blockGetter, biomeGetter, structureGetter, featureGetter, preset, FlatLevelGeneratorSettings.getDefault(biomeGetter, structureGetter, featureGetter));
    }

    static {
        Registry.register(REGISTRY, RLUtil.id("flat"), CODEC);
    }
}
