package com.iafenvoy.resourceworld.util;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public interface MinecraftServerAccessor {
    void resource_world$recreateWorld(RegistryKey<World> key);
}
