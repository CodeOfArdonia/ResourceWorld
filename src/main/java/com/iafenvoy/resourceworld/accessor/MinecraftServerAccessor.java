package com.iafenvoy.resourceworld.accessor;

import com.iafenvoy.resourceworld.config.generate.GenerateOption;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public interface MinecraftServerAccessor {
    boolean resourceWorld$createLevel(ResourceKey<Level> key, GenerateOption option);

    void resourceWorld$removeWorld(ResourceKey<Level> key);
}
