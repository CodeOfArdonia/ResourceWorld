package com.iafenvoy.resourceworld.data;

import com.iafenvoy.resourceworld.ResourceWorld;
import com.iafenvoy.resourceworld.config.ResourceWorldData;
import com.iafenvoy.resourceworld.config.WorldConfig;
import com.iafenvoy.resourceworld.util.MinecraftServerAccessor;
import com.iafenvoy.resourceworld.util.Timeout;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class ResourceWorldHelper {
    public static final Set<RegistryKey<World>> RESETTING = new HashSet<>();

    public static boolean isNotResourceWorld(RegistryKey<World> key) {
        return !key.getValue().getNamespace().equals(ResourceWorld.MOD_ID);
    }

    public static String resolveId(RegistryKey<World> key) {
        return key.getValue().getPath();
    }

    public static RegistryKey<World> toRegistryKey(String id) {
        return RegistryKey.of(RegistryKeys.WORLD, Identifier.of(ResourceWorld.MOD_ID, id));
    }

    public static WorldSavePath getDimensionFolder(RegistryKey<World> key) {
        return new WorldSavePath("dimensions/%s/%s".formatted(key.getValue().getNamespace(), key.getValue().getPath()));
    }

    public static boolean createWorld(MinecraftServer server, RegistryKey<World> key, Identifier worldOption, long seed) {
        ResourceWorldData data = WorldConfig.create(resolveId(key), worldOption);
        if (seed != 0) data.setSeed(seed);
        else WorldConfig.newSeed(key);
        return recreateWorld(server, key, worldOption);
    }

    public static boolean recreateWorld(MinecraftServer server, RegistryKey<World> key, Identifier worldOption) {
        return ((MinecraftServerAccessor) server).resource_world$createWorld(key, worldOption);
    }

    public static void teleportOut(ServerWorld world) {
        MinecraftServer server = world.getServer();
        ServerWorld overworld = server.getOverworld();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
            if (player.getWorld().getRegistryKey().equals(world.getRegistryKey())) {
                BlockPos spawnPoint = player.getSpawnPointPosition();
                ServerWorld spawnDimension = server.getWorld(player.getSpawnPointDimension());
                if (spawnPoint == null) {
                    spawnPoint = overworld.getSpawnPos();
                    spawnDimension = overworld;
                }
                player.teleport(spawnDimension, spawnPoint.getX() + 0.5, spawnPoint.getY(), spawnPoint.getZ() + 0.5, player.getYaw(), player.getPitch());
            }
    }

    public static void deleteWorld(MinecraftServer server, ServerWorld world) {
        ResourceWorldHelper.unloadAndDelete(world);
        WorldConfig.remove(world.getRegistryKey());
        Timeout.create(0, () -> ((MinecraftServerAccessor) server).resource_world$getWorlds().remove(world.getRegistryKey()));
    }

    public static void reset(ServerWorld world) {
        ResourceWorldData data = WorldConfig.get(world.getRegistryKey());
        if (data == null) return;
        RegistryKey<World> key = world.getRegistryKey();
        if (RESETTING.contains(key)) return;
        RESETTING.add(key);
        MinecraftServer server = world.getServer();
        printInfo(server, "Reset in 3 seconds.", key);
        Timeout.create(60, () -> {
            WorldConfig.newSeed(key);
            unloadAndDelete(world);
            printInfo(server, "Creating new world...", key);
            recreateWorld(server, key, data.getTargetWorld());
            printInfo(server, "Reset complete!", key);
            world.savingDisabled = false;
            RESETTING.remove(key);
        });
    }

    private static void deleteWorldData(MinecraftServer server, RegistryKey<World> key) {
        try {
            Path path = server.getSavePath(getDimensionFolder(key));
            FileUtils.cleanDirectory(path.resolve("region").toFile());
            FileUtils.cleanDirectory(path.resolve("entities").toFile());
            FileUtils.cleanDirectory(path.resolve("poi").toFile());
            printInfo(server, "Successfully remove world data.", key);
        } catch (Exception e) {
            printError("Failed to remove world data.", key, e);
        }
    }

    private static void unloadAndDelete(ServerWorld world) {
        RegistryKey<World> key = world.getRegistryKey();
        MinecraftServer server = world.getServer();
        world.savingDisabled = true;
        printInfo(server, "Kicking player from world...", key);
        teleportOut(world);
        printInfo(server, "Unloading world...", key);
        unloadWorld(world);
        printInfo(server, "Removing world data from disk...", key);
        deleteWorldData(server, key);
    }

    private static void unloadWorld(ServerWorld world) {
        try {
            world.close();
        } catch (Exception e) {
            ResourceWorldHelper.printError("Failed to close world.", world.getRegistryKey(), e);
        }
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
