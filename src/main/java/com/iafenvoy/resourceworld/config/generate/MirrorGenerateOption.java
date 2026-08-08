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
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.dimension.LevelStem;

public record MirrorGenerateOption(ResourceKey<LevelStem> dimension) implements GenerateOption {
    public static final /*? >=1.20.5 {*/MapCodec/*?} else {*//*Codec*//*?}*/<MirrorGenerateOption> CODEC = RecordCodecBuilder./*? >=1.20.5 {*/mapCodec/*?} else {*//*create*//*?}*/(i -> i.group(
            ResourceKey.codec(Registries.LEVEL_STEM).fieldOf("dimension").forGetter(MirrorGenerateOption::dimension)
    ).apply(i, MirrorGenerateOption::new));

    @Override
    public /*? >=1.20.5 {*/MapCodec/*?} else {*//*Codec*//*?}*/<MirrorGenerateOption> codec() {
        return CODEC;
    }

    @Override
    public LevelStem createStem(RegistryAccess registries) {
        return registries.lookupOrThrow(Registries.LEVEL_STEM).getOrThrow(this.dimension).value();
    }

    @Override
    public Identifier getDimensionTypeId() {
        return this.dimension.identifier();
    }
}
