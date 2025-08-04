package com.iafenvoy.resourceworld.data;

import com.google.common.collect.ImmutableMap;
import com.iafenvoy.resourceworld.config.ResourceWorldData;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.BiFunction;

public interface RandomTeleportEntrypoint {
    void register(ImmutableMap.Builder<Identifier, BiFunction<World, ResourceWorldData, BlockPos>> builder);
}
