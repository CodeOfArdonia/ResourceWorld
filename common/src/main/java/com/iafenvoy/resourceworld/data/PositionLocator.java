package com.iafenvoy.resourceworld.data;

import com.iafenvoy.resourceworld.util.RandomHelper;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
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
        return Optional.ofNullable(LOCATOR.get(world.getRegistryKey())).map(x -> x.apply(world, data)).map(pos -> {
            while (!world.isOutOfHeightLimit(pos) && (!world.getBlockState(pos.up()).isAir() || !world.getBlockState(pos).isAir() || !world.getBlockState(pos.down()).isSolidBlock(world, pos.down())))
                pos = pos.down();
            return world.isOutOfHeightLimit(pos) ? null : pos.up();
        }).orElse(null);
    }

    public static BlockPos randomPos(World world, SingleWorldData data, int y) {
        Random random = new Random(world.random.nextLong());
        return new BlockPos(RandomHelper.nextInt(random, data.getCenterX() - data.getRange(), data.getCenterX() + data.getRange()),
                y, RandomHelper.nextInt(random, data.getCenterZ() - data.getRange(), data.getCenterZ() + data.getRange()));
    }

    static {
        LOCATOR.put(RWDimensions.RESOURCE_WORLD, (world, data) -> randomPos(world, data, world.getTopY()));
        LOCATOR.put(RWDimensions.RESOURCE_NETHER, (world, data) -> randomPos(world, data, 125));
        LOCATOR.put(RWDimensions.RESOURCE_END, (world, data) ->
                data.getCenterX() - data.getRange() < 0 && data.getCenterX() + data.getRange() > 0 &&
                        data.getCenterZ() - data.getRange() < 0 && data.getCenterZ() + data.getRange() > 0
                        ? BlockPos.ORIGIN.up(world.getTopY() - 1) : null);
    }
}
