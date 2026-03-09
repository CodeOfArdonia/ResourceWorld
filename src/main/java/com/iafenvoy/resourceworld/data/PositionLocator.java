package com.iafenvoy.resourceworld.data;

import com.google.common.collect.ImmutableMap;
import com.iafenvoy.integration.entrypoint.EntryPointManager;
import com.iafenvoy.resourceworld.config.ResourceWorldData;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class PositionLocator {
    private static final Map<ResourceLocation, BiFunction<Level, ResourceWorldData, BlockPos>> LOCATOR;

    @Nullable
    public static BlockPos locate(Level world, ResourceWorldData data) {
        return LOCATOR.getOrDefault(data.getTargetWorld(), (w, d) -> toValidPos(randomPos(w, d, w.getMaxBuildHeight()), w)).apply(world, data);
    }

    @Nullable
    public static BlockPos toValidPos(BlockPos pos, Level world) {
        if (pos == null || world.getChunk(pos) == null) return null;
        while (pos.getY() > world.getMinBuildHeight() && (world.getBlockState(pos.below()).isAir() || !world.getBlockState(pos).isAir() || !world.getBlockState(pos.above()).isAir()))
            pos = pos.below();
        return pos.getY() > world.getMinBuildHeight() ? pos : null;
    }

    public static BlockPos randomPos(Level world, ResourceWorldData data, int y) {
        Random random = new Random(world.random.nextLong());
        ResourceWorldData.Settings settings = data.getSettings();
        Optional<BlockPos> pos = settings.getSpawnPoint();
        return pos.orElseGet(() -> new BlockPos(randomInt(random, settings.getCenterX() - settings.getRange(), settings.getCenterX() + settings.getRange()),
                y, randomInt(random, settings.getCenterZ() - settings.getRange(), settings.getCenterZ() + settings.getRange())));
    }

    public static int randomInt(Random random, int min, int max) {
        return min >= max ? min : random.nextInt(max - min + 1) + min;
    }

    static {
        ImmutableMap.Builder<ResourceLocation, BiFunction<Level, ResourceWorldData, BlockPos>> builder = ImmutableMap.builder();
        builder.put(Level.NETHER.location(), (world, data) -> {
            BlockPos pos = toValidPos(randomPos(world, data, 100), world);
            if (pos != null && world.getBlockState(pos.below()).liquid())
                world.setBlockAndUpdate(pos.below(), Blocks.NETHERRACK.defaultBlockState());
            return pos;
        });
        builder.put(Level.END.location(), (world, data) -> toValidPos(world.getWorldBorder().isWithinBounds(0, 0) ? BlockPos.ZERO.above(world.getMaxBuildHeight() - 1) : null, world));
        EntryPointManager.getEntryPoints("random_teleport_locator", RandomTeleportEntrypoint.class).forEach(x -> x.register(builder));
        LOCATOR = builder.buildKeepingLast();
    }
}
