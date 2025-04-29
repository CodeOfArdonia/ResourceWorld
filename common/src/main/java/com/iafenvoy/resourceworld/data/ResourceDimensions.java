package com.iafenvoy.resourceworld.data;

import com.iafenvoy.resourceworld.ResourceWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;

public final class ResourceDimensions {
    public static final RegistryKey<World> RESOURCE_WORLD = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(ResourceWorld.MOD_ID, "overworld"));
    public static final RegistryKey<World> RESOURCE_NETHER = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(ResourceWorld.MOD_ID, "the_nether"));
    public static final RegistryKey<World> RESOURCE_END = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(ResourceWorld.MOD_ID, "the_end"));

    public static WorldSavePath getDimensionFolder(RegistryKey<World> key) {
        return new WorldSavePath("dimensions/%s/%s".formatted(key.getValue().getNamespace(), key.getValue().getPath()));
    }

    public static RegistryKey<World> toResourceWorldKey(RegistryKey<World> key) {
        return RegistryKey.of(RegistryKeys.WORLD, Identifier.of(ResourceWorld.MOD_ID, key.getValue().getPath()));
    }
}
