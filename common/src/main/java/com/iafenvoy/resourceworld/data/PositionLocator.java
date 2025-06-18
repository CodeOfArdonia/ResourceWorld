package com.iafenvoy.resourceworld.data;

import com.iafenvoy.resourceworld.config.ResourceWorldData;
import net.minecraft.util.Identifier;
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
    private static final Map<Identifier, BiFunction<World, ResourceWorldData, BlockPos>> LOCATOR = new HashMap<>();

    @Nullable
    public static BlockPos locate(World world, ResourceWorldData data) {
        return Optional.ofNullable(LOCATOR.getOrDefault(data.getTargetWorld(), (w, d) -> randomPos(w, d, world.getTopY()))).map(x -> x.apply(world, data)).filter(x -> world.getChunk(x) != null).map(pos -> world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos)).filter(x -> x.getY() > world.getBottomY()).orElse(null);
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
        LOCATOR.put(World.NETHER.getValue(), (world, data) -> randomPos(world, data, 125));
        LOCATOR.put(World.END.getValue(), (world, data) -> world.getWorldBorder().contains(0, 0) ? BlockPos.ORIGIN.up(world.getTopY() - 1) : null);
    }
}
