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
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class PositionLocator {
    private static final Map<Identifier, BiFunction<Level, ResourceWorldData, BlockPos>> LOCATOR;

    @Nullable
    public static BlockPos locate(Level world, ResourceWorldData data) {
        return LOCATOR.getOrDefault(data.getGenerateOption().getDimensionTypeId(), (w, d) -> toValidPos(randomPos(w, d, w.getMaxY()), w)).apply(world, data);
    }

    @Nullable
    public static BlockPos toValidPos(BlockPos pos, Level world) {
        if (pos == null || world.getChunk(pos) == null) return null;
        while (pos.getY() > world.getMinY() && (world.getBlockState(pos.below()).isAir() || !world.getBlockState(pos).isAir() || !world.getBlockState(pos.above()).isAir()))
            pos = pos.below();
        return pos.getY() > world.getMinY() ? pos : null;
    }

    public static BlockPos randomPos(Level world, ResourceWorldData data, int y) {
        Random random = new Random(world.getRandom().nextLong());
        ResourceWorldData.Settings settings = data.getSettings();
        Optional<BlockPos> pos = settings.getSpawnPoint();
        return pos.orElseGet(() -> new BlockPos(randomInt(random, settings.getCenterX() - settings.getRange(), settings.getCenterX() + settings.getRange()),
                y, randomInt(random, settings.getCenterZ() - settings.getRange(), settings.getCenterZ() + settings.getRange())));
    }

    public static int randomInt(Random random, int min, int max) {
        return min >= max ? min : random.nextInt(max - min + 1) + min;
    }

    static {
        ImmutableMap.Builder<Identifier, BiFunction<Level, ResourceWorldData, BlockPos>> builder = ImmutableMap.builder();
        builder.put(Level.NETHER.identifier(), (world, data) -> {
            BlockPos pos = toValidPos(randomPos(world, data, 100), world);
            if (pos != null && !world.getFluidState(pos.below()).isEmpty())
                world.setBlockAndUpdate(pos.below(), Blocks.NETHERRACK.defaultBlockState());
            return pos;
        });
        builder.put(Level.END.identifier(), (world, data) -> toValidPos(world.getWorldBorder().isWithinBounds(0, 0) ? BlockPos.ZERO.above(world.getMaxY() - 1) : null, world));
        EntryPointManager.getEntryPoints("random_teleport_locator", RandomTeleportEntrypoint.class).forEach(x -> x.register(builder));
        LOCATOR = builder.buildKeepingLast();
    }
}
