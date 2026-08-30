package com.iafenvoy.resourceworld.config.generate;

import com.iafenvoy.resourceworld.util.RLUtil;
import com.mojang.serialization.Codec;
//? >=1.20.5 {
import com.mojang.serialization.MapCodec;
//?}
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.ArrayList;
import java.util.List;

public record FlatGenerateOption(String preset) implements GenerateOption {
    public static final /*? >=1.20.5 {*/ MapCodec/*?} else {*//*Codec*//*?}*/<FlatGenerateOption> CODEC = RecordCodecBuilder./*? >=1.20.5 {*/mapCodec/*?} else {*//*create*//*?}*/(i -> i.group(
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
        FlatLevelGeneratorSettings defaults = FlatLevelGeneratorSettings.getDefault(biomeGetter, structureGetter, featureGetter);
        String[] sections = preset.split(";", -1);
        if (sections.length == 0) return defaults;

        List<FlatLayerInfo> layers = new ArrayList<>();
        int layerStart = 0;
        for (String layer : sections[0].split(",", -1)) {
            FlatLayerInfo info = parseLayer(blockGetter, layer, layerStart);
            if (info == null) return defaults;
            layers.add(info);
            layerStart += info.getHeight();
        }
        if (layers.isEmpty()) return defaults;

        Holder<Biome> biome = biomeGetter.getOrThrow(Biomes.PLAINS);
        for (int i = 1; i < sections.length; i++) {
            ResourceLocation id = ResourceLocation.tryParse(sections[i]);
            if (id == null) continue;
            var holder = biomeGetter.get(net.minecraft.resources.ResourceKey.create(Registries.BIOME, id));
            if (holder.isPresent()) biome = holder.get();
        }
        return defaults.withBiomeAndLayers(layers, defaults.structureOverrides(), biome);
    }

    private static FlatLayerInfo parseLayer(HolderGetter<Block> blocks, String value, int start) {
        String[] parts = value.split("\\*", 2);
        int height = 1;
        String blockName = parts[0];
        if (parts.length == 2) {
            try {
                height = Math.max(0, Integer.parseInt(parts[0]));
            } catch (NumberFormatException ignored) {
                return null;
            }
            blockName = parts[1];
        }
        int layerHeight = Math.min(height, DimensionType.Y_SIZE - start);
        ResourceLocation id = ResourceLocation.tryParse(blockName);
        if (id == null) return null;
        return blocks.get(net.minecraft.resources.ResourceKey.create(Registries.BLOCK, id))
                .map(Holder.Reference::value)
                .map(block -> new FlatLayerInfo(layerHeight, block))
                .orElse(null);
    }

    @Override
    public ResourceLocation getDimensionTypeId() {
        return RLUtil.id(this.preset);
    }
}
