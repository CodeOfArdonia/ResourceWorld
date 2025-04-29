package com.iafenvoy.resourceworld.data;

import com.iafenvoy.resourceworld.config.SingleWorldData;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;

public class PositionLocator {
    private static final Map<RegistryKey<World>, BiFunction<World, SingleWorldData, BlockPos>> LOCATOR = new HashMap<>();

    @Nullable
    public static BlockPos locate(World world, SingleWorldData data) {
        return Optional.ofNullable(LOCATOR.get(world.getRegistryKey())).map(x -> x.apply(world, data)).filter(x -> world.getChunk(x) != null).map(pos -> world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, pos)).orElse(null);
    }

    public static BlockPos randomPos(World world, SingleWorldData data, int y) {
        Random random = new Random(world.random.nextLong());
        return new BlockPos(randomInt(random, data.getCenterX() - data.getRange(), data.getCenterX() + data.getRange()),
                y, randomInt(random, data.getCenterZ() - data.getRange(), data.getCenterZ() + data.getRange()));
    }

    public static int randomInt(Random random, int min, int max) {
        return min >= max ? min : random.nextInt(max - min + 1) + min;
    }

    static {
        LOCATOR.put(ResourceDimensions.RESOURCE_WORLD, (world, data) -> randomPos(world, data, world.getTopY()));
        LOCATOR.put(ResourceDimensions.RESOURCE_NETHER, (world, data) -> randomPos(world, data, 125));
        LOCATOR.put(ResourceDimensions.RESOURCE_END, (world, data) -> world.getWorldBorder().contains(0, 0) ? BlockPos.ORIGIN.up(world.getTopY() - 1) : null);
    }
}
