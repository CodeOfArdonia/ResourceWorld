package com.iafenvoy.resourceworld.accessor;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public interface MinecraftServerAccessor {
    boolean resource_world$createWorld(RegistryKey<World> key, Identifier worldOption);

    void resource_world$removeWorld(RegistryKey<World> key);
}
