package com.iafenvoy.resourceworld.data;

import com.google.gson.JsonParser;
import com.iafenvoy.resourceworld.ResourceWorld;
import com.mojang.serialization.JsonOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

public class WorldConfig {
    private static final WorldSavePath RESOURCE_WORLD_DATA = new WorldSavePath("resource_world.json");
    private static final Random RANDOM = new Random();
    private static final Map<RegistryKey<World>, Supplier<SingleWorldData>> BY_WORLD = new HashMap<>();
    @Nullable
    private static MinecraftServer SERVER;
    @Nullable
    private static ResourceWorldData DATA;

    @ApiStatus.Internal
    public static void bootstrap(MinecraftServer server) {
        SERVER = server;
        try {
            DATA = ResourceWorldData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(FileUtils.readFileToString(SERVER.getSavePath(RESOURCE_WORLD_DATA).toFile(), StandardCharsets.UTF_8))).resultOrPartial(ResourceWorld.LOGGER::error).orElseThrow();
        } catch (FileNotFoundException e) {
            DATA = new ResourceWorldData();
            DATA.getWorld().setSeed(randomSeed());
            DATA.getNether().setSeed(randomSeed());
            DATA.getEnd().setSeed(randomSeed());
            saveConfig();
        } catch (Exception e) {
            ResourceWorld.LOGGER.error("Failed to load config", e);
        }
    }

    @ApiStatus.Internal
    public static void stop() {
        saveConfig();
        SERVER = null;
    }

    @Nullable
    public static SingleWorldData getData(RegistryKey<World> key) {
        return Optional.ofNullable(BY_WORLD.get(key)).map(Supplier::get).orElse(null);
    }

    public static long getSeed(RegistryKey<World> key) {
        return Optional.ofNullable(BY_WORLD.get(key)).map(Supplier::get).map(SingleWorldData::getSeed).orElse(0L);
    }

    public static void newSeed(RegistryKey<World> key) {
        Optional.ofNullable(BY_WORLD.get(key)).ifPresent(x -> x.get().setSeed(randomSeed()));
    }

    private static long randomSeed() {
        return RANDOM.nextLong();
    }

    private static void saveConfig() {
        if (SERVER == null) return;
        try {
            FileUtils.write(SERVER.getSavePath(RESOURCE_WORLD_DATA).toFile(), ResourceWorldData.CODEC.encodeStart(JsonOps.INSTANCE, DATA).resultOrPartial(ResourceWorld.LOGGER::error).orElseThrow().toString(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            ResourceWorld.LOGGER.error("Failed to create config", ex);
        }
    }

    static {
        BY_WORLD.put(RWDimensions.RESOURCE_WORLD, () -> DATA == null ? new SingleWorldData() : DATA.getWorld());
        BY_WORLD.put(RWDimensions.RESOURCE_NETHER, () -> DATA == null ? new SingleWorldData() : DATA.getNether());
        BY_WORLD.put(RWDimensions.RESOURCE_END, () -> DATA == null ? new SingleWorldData() : DATA.getEnd());
    }
}
