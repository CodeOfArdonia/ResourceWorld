package com.iafenvoy.resourceworld.data;

import com.iafenvoy.resourceworld.ResourceWorld;
import com.iafenvoy.resourceworld.accessor.MinecraftServerAccessor;
import com.iafenvoy.resourceworld.config.ResourceWorldData;
import com.iafenvoy.resourceworld.config.WorldConfig;
import com.iafenvoy.resourceworld.config.generate.GenerateOption;
import com.iafenvoy.resourceworld.config.generate.MirrorGenerateOption;
import com.iafenvoy.resourceworld.config.generate.TemplateGenerateOption;
import com.iafenvoy.resourceworld.mixin.LevelResourceAccessor;
import com.iafenvoy.resourceworld.util.RLUtil;
import com.iafenvoy.server.i18n.ServerI18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
//? >=1.20.5 {
import net.minecraft.nbt.NbtAccounter;
//?}
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
//? >=1.20.5 {
import net.minecraft.world.level.portal.DimensionTransition;
//?} else {
/*import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
*///?}
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ResourceWorldHelper {
    public static final Set<ResourceKey<Level>> RESETTING = new HashSet<>();

    public static boolean isResourceWorld(ResourceKey<Level> key) {
        return key.location().getNamespace().equals(ResourceWorld.MOD_ID);
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
        try {
            stem.createStem(server.registryAccess());
        } catch (Exception e) {
            ResourceWorld.LOGGER.error("Invalid resource world generation option", e);
            return false;
        }
        Long sourceSeed = getCopySeed(server, stem);
        if (isCopyOption(stem) && sourceSeed == null) return false;

        ResourceWorldData data = WorldConfig.create(resolveId(key), stem);
        if (sourceSeed != null) data.setSeed(sourceSeed);
        else if (seed != 0) data.setSeed(seed);
        else WorldConfig.newSeed(key);
        if (!copyWorldData(server, key, stem)) {
            WorldConfig.remove(key);
            return false;
        }
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
                teleportToSpawn(player);
    }

    public static void teleportToSpawn(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerLevel respawnLevel = server.getLevel(player.getRespawnDimension());
        if (respawnLevel != null && !isResourceWorld(respawnLevel.dimension()) && player.getRespawnPosition() != null) {
            //? >=1.20.5 {
            DimensionTransition transition = player.findRespawnPositionAndUseSpawnBlock(true, DimensionTransition.DO_NOTHING);
            if (!transition.missingRespawnBlock() && transition.newLevel() == respawnLevel) {
                player.teleportTo(respawnLevel, transition.pos().x, transition.pos().y, transition.pos().z, transition.yRot(), transition.xRot());
                return;
            }
            //?} else {
            /*Optional<Vec3> position = Player.findRespawnPositionAndUseSpawnBlock(respawnLevel, player.getRespawnPosition(), player.getRespawnAngle(), player.isRespawnForced(), true);
            if (position.isPresent()) {
                Vec3 vec3 = position.get();
                player.teleportTo(respawnLevel, vec3.x, vec3.y, vec3.z, player.getRespawnAngle(), 0.0F);
                return;
            }
            *///?}
        }

        if (isResourceWorld(player.getRespawnDimension()))
            player.setRespawnPosition(Level.OVERWORLD, null, 0, false, false);

        ServerLevel overworld = server.overworld();
        BlockPos spawn = overworld.getSharedSpawnPos();
        player.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, overworld.getSharedSpawnAngle(), 0.0F);
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
        if (!isCopyOption(data.getGenerateOption())) WorldConfig.newSeed(key);
        unloadAndDelete(world);
        if (!copyWorldData(server, key, data.getGenerateOption())) {
            ResourceWorld.LOGGER.error("Failed to copy data while resetting resource world {}", key.location());
            world.noSave = false;
            RESETTING.remove(key);
            return;
        }
        printInfo(server, "message.resource_world.creating", key);
        recreateWorld(server, key, data.getGenerateOption());
        printInfo(server, "message.resource_world.reset", key);
        world.noSave = false;
        RESETTING.remove(key);
    }

    private static void deleteWorldData(MinecraftServer server, ResourceKey<Level> key) {
        Path path = server.getWorldPath(getDimensionFolder(key));
        clearDataDirectory(path.resolve("region"), key, "region");
        clearDataDirectory(path.resolve("entities"), key, "entities");
        clearDataDirectory(path.resolve("poi"), key, "poi");
        printInfo(server, "message.resource_world.success_remove_world_data", key);
    }

    @SuppressWarnings("DeconstructionCanBeUsed")
    private static boolean copyWorldData(MinecraftServer server, ResourceKey<Level> target, GenerateOption option) {
        Path source;
        if (option instanceof MirrorGenerateOption copy) {
            ResourceKey<Level> sourceKey = ResourceKey.create(Registries.DIMENSION, copy.dimension().location());
            if (sourceKey.equals(target) || server.getLevel(sourceKey) == null) {
                ResourceWorld.LOGGER.error("Invalid copy-world source {}", sourceKey.location());
                return false;
            }
            server.saveAllChunks(true, true, true);
            source = getDimensionDataFolder(server.getWorldPath(LevelResource.ROOT), sourceKey.location());
        } else if (option instanceof TemplateGenerateOption copy) {
            if (!copy.template().matches("[A-Za-z0-9_-]+")) {
                ResourceWorld.LOGGER.error("Invalid resource world template name {}", copy.template());
                return false;
            }
            source = getDimensionDataFolder(getTemplateRoot(server).resolve(copy.template()), copy.dimension().location());
        } else return true;

        if (!Files.isDirectory(source)) {
            ResourceWorld.LOGGER.error("Resource world copy source does not exist: {}", source);
            return false;
        }

        Path destination = server.getWorldPath(getDimensionFolder(target));
        try {
            clearDataDirectory(destination.resolve("region"), target, "region");
            clearDataDirectory(destination.resolve("entities"), target, "entities");
            clearDataDirectory(destination.resolve("poi"), target, "poi");
            for (String directory : new String[]{"region", "entities", "poi"}) {
                Path sourceDirectory = source.resolve(directory);
                if (Files.isDirectory(sourceDirectory)) FileUtils.copyDirectory(sourceDirectory.toFile(), destination.resolve(directory).toFile());
            }
            return true;
        } catch (Exception e) {
            ResourceWorld.LOGGER.error("Failed to copy resource world data from {}", source, e);
            return false;
        }
    }

    @SuppressWarnings("DeconstructionCanBeUsed")
    private static Long getCopySeed(MinecraftServer server, GenerateOption option) {
        if (option instanceof MirrorGenerateOption copy) {
            ServerLevel source = server.getLevel(ResourceKey.create(Registries.DIMENSION, copy.dimension().location()));
            return source == null ? null : source.getSeed();
        }
        if (option instanceof TemplateGenerateOption copy) {
            if (!copy.template().matches("[A-Za-z0-9_-]+")) return null;
            try {
                Path levelData = getTemplateRoot(server).resolve(copy.template()).resolve("level.dat");
                //? >=1.20.5 {
                CompoundTag data = NbtIo.readCompressed(levelData, NbtAccounter.unlimitedHeap()).getCompound("Data");
                //?} else {
                /*CompoundTag data = NbtIo.readCompressed(levelData.toFile()).getCompound("Data");
                *///?}
                CompoundTag worldGenSettings = data.getCompound("WorldGenSettings");
                return worldGenSettings.contains("seed") ? worldGenSettings.getLong("seed") : null;
            } catch (Exception e) {
                ResourceWorld.LOGGER.error("Failed to read resource world template seed", e);
            }
        }
        return null;
    }

    private static boolean isCopyOption(GenerateOption option) {
        return option instanceof MirrorGenerateOption || option instanceof TemplateGenerateOption;
    }

    private static Path getTemplateRoot(MinecraftServer server) {
        //? >=1.21 {
        return server.getServerDirectory().resolve("resourceworld");
        //?} else {
        /*return server.getServerDirectory().toPath().resolve("resourceworld");
        *///?}
    }

    private static Path getDimensionDataFolder(Path root, ResourceLocation dimension) {
        if (dimension.equals(Level.OVERWORLD.location())) return root;
        if (dimension.equals(Level.NETHER.location())) return root.resolve("DIM-1");
        if (dimension.equals(Level.END.location())) return root.resolve("DIM1");
        return root.resolve("dimensions").resolve(dimension.getNamespace()).resolve(dimension.getPath());
    }

    private static void clearDataDirectory(Path directory, ResourceKey<Level> key, String name) {
        if (!Files.isDirectory(directory)) return;
        try {
            FileUtils.cleanDirectory(directory.toFile());
        } catch (Exception e) {
            printError("Failed to remove %s data.".formatted(name), key, e);
        }
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
        ResourceWorld.LOGGER.info(ServerI18n.translateDefault("message.resource_world.base", world.location().getPath(), ServerI18n.translateDefault(key)));
        for (ServerPlayer player : server.getPlayerList().getPlayers())
            player.sendSystemMessage(ServerI18n.translateToLiteral(player, "message.resource_world.base", world.location().getPath(), ServerI18n.translate(player, key)));
    }

    private static void printError(String s, ResourceKey<Level> world, Object... data) {
        ResourceWorld.LOGGER.info("[Resource World] (World: %s) %s".formatted(world.location().getPath(), s), data);
    }
}
