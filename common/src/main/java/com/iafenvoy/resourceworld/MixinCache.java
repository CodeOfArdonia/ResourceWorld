package com.iafenvoy.resourceworld;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class MixinCache {
    @Nullable
    public static RegistryKey<World> CURRENT_TICKING_WORLD;
}
