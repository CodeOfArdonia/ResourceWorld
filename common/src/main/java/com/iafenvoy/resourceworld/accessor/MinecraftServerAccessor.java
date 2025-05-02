package com.iafenvoy.resourceworld.accessor;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Map;

public interface MinecraftServerAccessor {
    boolean resource_world$createWorld(RegistryKey<World> key, Identifier worldOption);

    Map<RegistryKey<World>, ServerWorld> resource_world$getWorlds();
}
