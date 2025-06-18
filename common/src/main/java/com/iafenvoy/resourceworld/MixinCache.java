package com.iafenvoy.resourceworld;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class MixinCache {
    @Nullable
    public static RegistryKey<World> CURRENT_TICKING_WORLD;
    public static final List<Consumer<MinecraftServer>> WORLD_CHANGE_CALLBACKS = new CopyOnWriteArrayList<>();
}
