package com.iafenvoy.resourceworld;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public final class MixinCache {
    @Nullable
    public static MinecraftServer SERVER;
    @Nullable
    public static ResourceKey<Level> CURRENT_TICKING_WORLD;
    private static final Map<ServerLevelData, ResourceKey<Level>> RESOURCE_WORLD_DATA = new WeakHashMap<>();

    public static void registerResourceWorldData(ServerLevelData data, ResourceKey<Level> key) {
        RESOURCE_WORLD_DATA.put(data, key);
    }

    @Nullable
    public static ResourceKey<Level> getResourceWorldKey(ServerLevelData data) {
        return RESOURCE_WORLD_DATA.get(data);
    }
}
