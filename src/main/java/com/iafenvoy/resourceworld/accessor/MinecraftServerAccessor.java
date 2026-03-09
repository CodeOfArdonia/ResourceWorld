package com.iafenvoy.resourceworld.accessor;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public interface MinecraftServerAccessor {
    boolean resourceWorld$createLevel(ResourceKey<Level> key, ResourceLocation worldOption);

    void resourceWorld$removeWorld(ResourceKey<Level> key);
}
