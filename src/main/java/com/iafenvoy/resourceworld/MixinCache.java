package com.iafenvoy.resourceworld;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class MixinCache {
    @Nullable
    public static MinecraftServer SERVER;
    @Nullable
    public static ResourceKey<Level> CURRENT_TICKING_WORLD;
    public static final List<Consumer<MinecraftServer>> WORLD_CHANGE_CALLBACKS = new CopyOnWriteArrayList<>();
}
