package com.iafenvoy.resourceworld.data;

import com.iafenvoy.resourceworld.ResourceWorld;
import com.iafenvoy.resourceworld.util.ThreadedAnvilChunkStorageAccessor;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class WorldReset {
    public static final Set<RegistryKey<World>> RESETTING = new HashSet<>();

    public static CompletableFuture<Void> reset(ServerWorld world) {
        if (WorldConfig.getData(world.getRegistryKey()) == null) return CompletableFuture.runAsync(() -> {
        });
        return CompletableFuture.runAsync(() -> {
            RegistryKey<World> key = world.getRegistryKey();
            if (RESETTING.contains(key)) return;
            RESETTING.add(key);
            world.savingDisabled = true;
            printInfo("Kicking player from world...", key);
            MinecraftServer server = world.getServer();
            ServerWorld overworld = server.getOverworld();
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
                if (player.getWorld().getRegistryKey().equals(world.getRegistryKey())) {
                    BlockPos spawnPoint = world.getSpawnPos();
                    player.teleport(overworld, spawnPoint.getX() + 0.5, spawnPoint.getY(), spawnPoint.getZ() + 0.5, player.getYaw(), player.getPitch());
                }
            printInfo("Recreating generation settings...", key);
            WorldConfig.newSeed(key);
            ((ThreadedAnvilChunkStorageAccessor) world.getChunkManager().threadedAnvilChunkStorage).resource_world$recreateGenerationSettings();
            printInfo("Removing world data from disk...", key);
            ((ThreadedAnvilChunkStorageAccessor) world.getChunkManager().threadedAnvilChunkStorage).resource_world$clearChunks();
            try {
                Path path = server.getSavePath(RWDimensions.getDimensionFolder(world.getRegistryKey()));
                FileUtils.cleanDirectory(path.resolve("entities").toFile());
                FileUtils.cleanDirectory(path.resolve("poi").toFile());
                FileUtils.cleanDirectory(path.resolve("region").toFile());
                printInfo("Successfully remove world data.", key);
            } catch (Exception e) {
                printInfo("Failed to remove world data.", key, e);
            }
            ((ThreadedAnvilChunkStorageAccessor) world.getChunkManager().threadedAnvilChunkStorage).resource_world$clearChunks();
            printInfo("Reset complete!", key);
            world.savingDisabled = false;
            RESETTING.remove(key);
        });
    }

    private static void printInfo(String s, RegistryKey<World> world, Object... data) {
        ResourceWorld.LOGGER.info("[Resource World] (World: %s) %s".formatted(world.getValue(), s), data);
    }
}
