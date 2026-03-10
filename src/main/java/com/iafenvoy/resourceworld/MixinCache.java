package com.iafenvoy.resourceworld;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class MixinCache {
    @Nullable
    public static MinecraftServer SERVER;
    @Nullable
    public static ResourceKey<Level> CURRENT_TICKING_WORLD;
}
