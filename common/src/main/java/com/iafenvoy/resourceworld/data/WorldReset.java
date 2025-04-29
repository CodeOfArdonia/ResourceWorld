package com.iafenvoy.resourceworld.data;

import com.iafenvoy.resourceworld.ResourceWorld;
import com.iafenvoy.resourceworld.config.WorldConfig;
import com.iafenvoy.resourceworld.util.MinecraftServerAccessor;
import com.iafenvoy.resourceworld.util.Timeout;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public final class WorldReset {
    public static final Set<RegistryKey<World>> RESETTING = new HashSet<>();

    public static void reset(ServerWorld world) {
        if (WorldConfig.getData(world.getRegistryKey()) == null) return;
        RegistryKey<World> key = world.getRegistryKey();
        MinecraftServer server = world.getServer();
        printInfo(server, "Reset in 3 seconds.", key);
        Timeout.create(60, () -> {
            if (RESETTING.contains(key)) return;
            RESETTING.add(key);
            WorldConfig.newSeed(key);
            world.savingDisabled = true;
            MinecraftServerAccessor accessor = (MinecraftServerAccessor) server;
            printInfo(server, "Kicking player from world...", key);
            ServerWorld overworld = server.getOverworld();
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
                if (player.getWorld().getRegistryKey().equals(world.getRegistryKey())) {
                    BlockPos spawnPoint = player.getSpawnPointPosition();
                    ServerWorld spawnDimension = server.getWorld(player.getSpawnPointDimension());
                    if (spawnPoint == null) {
                        spawnPoint = world.getSpawnPos();
                        spawnDimension = overworld;
                    }
                    player.teleport(spawnDimension, spawnPoint.getX() + 0.5, spawnPoint.getY(), spawnPoint.getZ() + 0.5, player.getYaw(), player.getPitch());
                }
            printInfo(server, "Unloading world...", key);
            try {
                world.close();
            } catch (Exception e) {
                printError("Failed to close world.", key, e);
            }
            printInfo(server, "Removing world data from disk...", key);
            try {
                Path path = server.getSavePath(ResourceDimensions.getDimensionFolder(world.getRegistryKey()));
                FileUtils.cleanDirectory(path.resolve("entities").toFile());
                FileUtils.cleanDirectory(path.resolve("poi").toFile());
                FileUtils.cleanDirectory(path.resolve("region").toFile());
                printInfo(server, "Successfully remove world data.", key);
            } catch (Exception e) {
                printError("Failed to remove world data.", key, e);
            }
            printInfo(server, "Creating new world...", key);
            accessor.resource_world$recreateWorld(key);
            printInfo(server, "Reset complete!", key);
            world.savingDisabled = false;
            RESETTING.remove(key);
        });
    }

    private static void printInfo(MinecraftServer server, String s, RegistryKey<World> world) {
        String string = "[Resource World] (World: %s) %s".formatted(world.getValue().getPath(), s);
        ResourceWorld.LOGGER.info(string);
        server.getPlayerManager().broadcast(Text.literal(string), false);
    }

    private static void printError(String s, RegistryKey<World> world, Object... data) {
        ResourceWorld.LOGGER.info("[Resource World] (World: %s) %s".formatted(world.getValue().getPath(), s), data);
    }
}
