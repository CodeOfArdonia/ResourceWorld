package com.iafenvoy.resourceworld.config.generate;

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

public record TypeGenerateOption(ResourceKey<LevelStem> target) implements GenerateOption {
    public static final /*? >=1.20.5 {*/MapCodec/*?} else {*//*Codec*//*?}*/<TypeGenerateOption> CODEC = RecordCodecBuilder./*? >=1.20.5 {*/mapCodec/*?} else {*//*create*//*?}*/(i -> i.group(
            ResourceKey.codec(Registries.LEVEL_STEM).fieldOf("target").forGetter(TypeGenerateOption::target)
    ).apply(i, TypeGenerateOption::new));

    @Override
    public /*? >=1.20.5 {*/MapCodec/*?} else {*//*Codec*//*?}*/<TypeGenerateOption> codec() {
        return CODEC;
    }

    @Override
    public LevelStem createStem(RegistryAccess registries) {
        return registries.registryOrThrow(Registries.LEVEL_STEM).getOrThrow(this.target);
    }

    @Override
    public ResourceLocation getDimensionTypeId() {
        return this.target.location();
    }
}
