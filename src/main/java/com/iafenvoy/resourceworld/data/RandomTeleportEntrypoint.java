package com.iafenvoy.resourceworld.data;

import com.google.common.collect.ImmutableMap;
import com.iafenvoy.integration.entrypoint.IntegrationEntryPoint;
import com.iafenvoy.resourceworld.config.ResourceWorldData;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public interface RandomTeleportEntrypoint extends IntegrationEntryPoint {
    void register(ImmutableMap.Builder<ResourceLocation, BiFunction<Level, ResourceWorldData, BlockPos>> builder);
}
