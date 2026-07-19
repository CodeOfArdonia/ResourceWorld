package com.iafenvoy.resourceworld.config.generate;

import com.mojang.serialization.Codec;
//? >=1.20.5 {
import com.mojang.serialization.MapCodec;
//?} else {
/*import com.mojang.serialization.Codec;
 *///?}
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.LevelStem;

public record TemplateGenerateOption(String template, ResourceKey<LevelStem> dimension) implements GenerateOption {
    public static final /*? >=1.20.5 {*/MapCodec/*?} else {*//*Codec*//*?}*/<TemplateGenerateOption> CODEC = RecordCodecBuilder./*? >=1.20.5 {*/mapCodec/*?} else {*//*create*//*?}*/(i -> i.group(
            Codec.STRING.fieldOf("template").forGetter(TemplateGenerateOption::template),
            ResourceKey.codec(Registries.LEVEL_STEM).fieldOf("dimension").forGetter(TemplateGenerateOption::dimension)
    ).apply(i, TemplateGenerateOption::new));

    @Override
    public /*? >=1.20.5 {*/MapCodec/*?} else {*//*Codec*//*?}*/<TemplateGenerateOption> codec() {
        return CODEC;
    }

    @Override
    public LevelStem createStem(RegistryAccess registries) {
        return registries.registryOrThrow(Registries.LEVEL_STEM).getOrThrow(this.dimension);
    }

    @Override
    public ResourceLocation getDimensionTypeId() {
        return this.dimension.location();
    }
}
