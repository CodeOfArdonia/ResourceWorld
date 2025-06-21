package com.iafenvoy.resourceworld.data;

import com.iafenvoy.resourceworld.ResourceWorld;
import com.iafenvoy.resourceworld.accessor.MinecraftServerAccessor;
import com.iafenvoy.resourceworld.config.ResourceWorldData;
import com.iafenvoy.resourceworld.config.WorldConfig;
import com.iafenvoy.server.i18n.ServerI18n;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
        WorldConfig.remove(world.getRegistryKey());
        unloadAndDelete(world);
        ((MinecraftServerAccessor) server).resource_world$removeWorld(world.getRegistryKey());
    }

    public static void reset(ServerWorld world) {
        ResourceWorldData data = WorldConfig.get(world.getRegistryKey());
        if (data == null) return;
        RegistryKey<World> key = world.getRegistryKey();
        if (RESETTING.contains(key)) return;
        RESETTING.add(key);
        MinecraftServer server = world.getServer();
        WorldConfig.newSeed(key);
        unloadAndDelete(world);
        printInfo(server, "message.resource_world.creating", key);
        recreateWorld(server, key, data.getTargetWorld());
        printInfo(server, "message.resource_world.reset", key);
        world.savingDisabled = false;
        RESETTING.remove(key);
    }

    private static void deleteWorldData(MinecraftServer server, RegistryKey<World> key) {
        Path path = server.getSavePath(getDimensionFolder(key));
        try {
            FileUtils.cleanDirectory(path.resolve("region").toFile());
        } catch (Exception e) {
            printError("Failed to remove region data.", key, e);
        }
        try {
            FileUtils.cleanDirectory(path.resolve("entities").toFile());
        } catch (Exception e) {
            printError("Failed to remove entities data.", key, e);
        }
        try {
            FileUtils.cleanDirectory(path.resolve("poi").toFile());
        } catch (Exception e) {
            printError("Failed to remove poi data.", key, e);
        }
        printInfo(server, "message.resource_world.success_remove_world_data", key);
    }

    private static void unloadAndDelete(ServerWorld world) {
        RegistryKey<World> key = world.getRegistryKey();
        MinecraftServer server = world.getServer();
        world.savingDisabled = true;
        printInfo(server, "message.resource_world.kick_players", key);
        teleportOut(world);
        printInfo(server, "message.resource_world.unload_world", key);
        unloadWorld(world);
        printInfo(server, "message.resource_world.remove_world_data", key);
        deleteWorldData(server, key);
    }

    private static void unloadWorld(ServerWorld world) {
        try {
            ((MinecraftServerAccessor) world.getServer()).resource_world$removeWorld(world.getRegistryKey());
        } catch (Exception e) {
            printError("Failed to close world.", world.getRegistryKey(), e);
        }
    }

    private static void printInfo(MinecraftServer server, String key, RegistryKey<World> world) {
        ResourceWorld.LOGGER.info(ServerI18n.translate(ServerI18n.DEFAULT_LANGUAGE, "message.resource_world.base", world.getValue().getPath(), ServerI18n.translate(ServerI18n.DEFAULT_LANGUAGE, key)));
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
            player.sendMessage(ServerI18n.translateToLiteral(player, "message.resource_world.base", world.getValue().getPath(), ServerI18n.translate(player, key)));
    }

    private static void printError(String s, RegistryKey<World> world, Object... data) {
        ResourceWorld.LOGGER.info("[Resource World] (World: %s) %s".formatted(world.getValue().getPath(), s), data);
    }
}
