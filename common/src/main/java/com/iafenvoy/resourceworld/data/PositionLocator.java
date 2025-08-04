package com.iafenvoy.resourceworld.data;

import com.google.common.collect.ImmutableMap;
import com.iafenvoy.resourceworld.config.ResourceWorldData;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;

public class PositionLocator {
    private static final Map<Identifier, BiFunction<World, ResourceWorldData, BlockPos>> LOCATOR;

    @Nullable
    public static BlockPos locate(World world, ResourceWorldData data) {
        return LOCATOR.getOrDefault(data.getTargetWorld(), (w, d) -> toValidPos(randomPos(w, d, w.getTopY()), w)).apply(world, data);
    }

    @Nullable
    public static BlockPos toValidPos(BlockPos pos, World world) {
        if (pos == null || world.getChunk(pos) == null) return null;
        while (pos.getY() > world.getBottomY() && (world.getBlockState(pos.down()).isAir() || !world.getBlockState(pos).isAir() || !world.getBlockState(pos.up()).isAir()))
            pos = pos.down();
        return pos.getY() > world.getBottomY() ? pos : null;
    }

    public static BlockPos randomPos(World world, ResourceWorldData data, int y) {
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
        ImmutableMap.Builder<Identifier, BiFunction<World, ResourceWorldData, BlockPos>> builder = ImmutableMap.builder();
        builder.put(World.NETHER.getValue(), (world, data) -> {
            BlockPos pos = toValidPos(randomPos(world, data, 100), world);
            if (pos != null && world.getBlockState(pos.down()).isLiquid())
                world.setBlockState(pos.down(), Blocks.NETHERRACK.getDefaultState());
            return pos;
        });
        builder.put(World.END.getValue(), (world, data) -> toValidPos(world.getWorldBorder().contains(0, 0) ? BlockPos.ORIGIN.up(world.getTopY() - 1) : null, world));
        EntryPointLoader.INSTANCE.getEntries().forEach(x -> x.register(builder));
        LOCATOR = builder.buildKeepingLast();
    }
}
