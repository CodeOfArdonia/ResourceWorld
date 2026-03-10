package com.iafenvoy.resourceworld.data;

import com.iafenvoy.resourceworld.ResourceWorld;
import com.iafenvoy.resourceworld.accessor.MinecraftServerAccessor;
import com.iafenvoy.resourceworld.config.ResourceWorldData;
import com.iafenvoy.resourceworld.config.WorldConfig;
import com.iafenvoy.resourceworld.config.generate.GenerateOption;
import com.iafenvoy.resourceworld.mixin.LevelResourceAccessor;
import com.iafenvoy.resourceworld.util.RLUtil;
import com.iafenvoy.server.i18n.ServerI18n;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ResourceWorldHelper {
    public static final Set<ResourceKey<Level>> RESETTING = new HashSet<>();

    public static boolean isNotResourceWorld(ResourceKey<Level> key) {
        return !key.location().getNamespace().equals(ResourceWorld.MOD_ID);
    }

    public static String resolveId(ResourceKey<Level> key) {
        return key.location().getPath();
    }

    public static ResourceKey<Level> toRegistryKey(String id) {
        return ResourceKey.create(Registries.DIMENSION, RLUtil.id(id));
    }

    public static LevelResource getDimensionFolder(ResourceKey<Level> key) {
        return LevelResourceAccessor.resourceWorld$newInstance("dimensions/%s/%s".formatted(key.location().getNamespace(), key.location().getPath()));
    }

    public static boolean createWorld(MinecraftServer server, ResourceKey<Level> key, GenerateOption stem, long seed) {
        ResourceWorldData data = WorldConfig.create(resolveId(key), stem);
        if (seed != 0) data.setSeed(seed);
        else WorldConfig.newSeed(key);
        return recreateWorld(server, key, stem);
    }

    public static boolean recreateWorld(MinecraftServer server, ResourceKey<Level> key, GenerateOption option) {
        return ((MinecraftServerAccessor) server).resourceWorld$createLevel(key, option);
    }

    public static void teleportOut(ServerLevel world) {
        MinecraftServer server = world.getServer();
        PlayerList playerList = server.getPlayerList();
        for (ServerPlayer player : playerList.getPlayers())
            if (Objects.equals(player.level().dimension(), world.dimension()))
                playerList.respawn(player, true/*? >=1.21 {*/, Entity.RemovalReason.CHANGED_DIMENSION/*?}*/);
    }

    public static void deleteWorld(MinecraftServer server, ServerLevel world) {
        WorldConfig.remove(world.dimension());
        unloadAndDelete(world);
        ((MinecraftServerAccessor) server).resourceWorld$removeWorld(world.dimension());
    }

    public static void reset(ServerLevel world) {
        ResourceWorldData data = WorldConfig.get(world.dimension());
        if (data == null) return;
        ResourceKey<Level> key = world.dimension();
        if (RESETTING.contains(key)) return;
        RESETTING.add(key);
        MinecraftServer server = world.getServer();
        WorldConfig.newSeed(key);
        unloadAndDelete(world);
        printInfo(server, "message.resource_world.creating", key);
        recreateWorld(server, key, data.getGenerateOption());
        printInfo(server, "message.resource_world.reset", key);
        world.noSave = false;
        RESETTING.remove(key);
    }

    private static void deleteWorldData(MinecraftServer server, ResourceKey<Level> key) {
        Path path = server.getWorldPath(getDimensionFolder(key));
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

    private static void unloadAndDelete(ServerLevel world) {
        ResourceKey<Level> key = world.dimension();
        MinecraftServer server = world.getServer();
        world.noSave = true;
        printInfo(server, "message.resource_world.kick_players", key);
        teleportOut(world);
        printInfo(server, "message.resource_world.unload_world", key);
        unloadWorld(world);
        printInfo(server, "message.resource_world.remove_world_data", key);
        deleteWorldData(server, key);
    }

    private static void unloadWorld(ServerLevel world) {
        try {
            ((MinecraftServerAccessor) world.getServer()).resourceWorld$removeWorld(world.dimension());
        } catch (Exception e) {
            printError("Failed to close world.", world.dimension(), e);
        }
    }

    private static void printInfo(MinecraftServer server, String key, ResourceKey<Level> world) {
        ResourceWorld.LOGGER.info(ServerI18n.translate(ServerI18n.DEFAULT_LANGUAGE, "message.resource_world.base", world.location().getPath(), ServerI18n.translate(ServerI18n.DEFAULT_LANGUAGE, key)));
        for (ServerPlayer player : server.getPlayerList().getPlayers())
            player.sendSystemMessage(ServerI18n.translateToLiteral(player, "message.resource_world.base", world.location().getPath(), ServerI18n.translate(player, key)));
    }

    private static void printError(String s, ResourceKey<Level> world, Object... data) {
        ResourceWorld.LOGGER.info("[Resource World] (World: %s) %s".formatted(world.location().getPath(), s), data);
    }
}
