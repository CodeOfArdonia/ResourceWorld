package com.iafenvoy.resourceworld.config;

import com.google.gson.JsonParser;
import com.iafenvoy.resourceworld.ResourceWorld;
import com.iafenvoy.resourceworld.data.ResourceWorldHelper;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
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
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class WorldConfig {
    private static final Codec<Map<String, ResourceWorldData>> CODEC = Codec.unboundedMap(Codec.STRING, ResourceWorldData.CODEC);
    private static final WorldSavePath RESOURCE_WORLD_DATA = new WorldSavePath("resource_world.json");
    private static final Random RANDOM = new Random();
    private static final Map<String, ResourceWorldData> DATA = new HashMap<>();
    @Nullable
    private static MinecraftServer SERVER;

    @ApiStatus.Internal
    public static void bootstrap(MinecraftServer server) {
        SERVER = server;
        DATA.clear();
        try {
            DATA.putAll(CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(FileUtils.readFileToString(server.getSavePath(RESOURCE_WORLD_DATA).toFile(), StandardCharsets.UTF_8))).resultOrPartial(ResourceWorld.LOGGER::error).orElseThrow());
        } catch (FileNotFoundException e) {
            saveConfig();
        } catch (Exception e) {
            ResourceWorld.LOGGER.error("Failed to load config", e);
        }
    }

    @ApiStatus.Internal
    public static void initResourceWorld(BiConsumer<RegistryKey<World>, Identifier> consumer) {
        for (Map.Entry<String, ResourceWorldData> entry : DATA.entrySet())
            consumer.accept(ResourceWorldHelper.toRegistryKey(entry.getKey()), entry.getValue().getTargetWorld());
    }

    public static CompletableFuture<Suggestions> appendSuggestions(SuggestionsBuilder builder) {
        DATA.keySet().forEach(builder::suggest);
        return builder.buildFuture();
    }

    @ApiStatus.Internal
    public static void stop() {
        saveConfig();
        SERVER = null;
    }

    public static ResourceWorldData create(String id, Identifier target) {
        ResourceWorldData data = new ResourceWorldData(target);
        data.setEnabled(true);
        DATA.put(id, data);
        saveConfig();
        return data;
    }

    @Nullable
    public static ResourceWorldData get(RegistryKey<World> key) {
        return DATA.get(ResourceWorldHelper.resolveId(key));
    }

    public static void remove(RegistryKey<World> key) {
        DATA.remove(ResourceWorldHelper.resolveId(key));
        saveConfig();
    }

    public static long getSeed(RegistryKey<World> key) {
        return Optional.ofNullable(get(key)).map(ResourceWorldData::getSeed).orElse(0L);
    }

    public static Optional<GameRules> getGameRules(RegistryKey<World> key) {
        return Optional.ofNullable(get(key)).map(ResourceWorldData::getGameRules);
    }

    public static Optional<Difficulty> getDifficulty(RegistryKey<World> key) {
        return Optional.ofNullable(get(key)).map(ResourceWorldData::getDifficulty);
    }

    public static void newSeed(RegistryKey<World> key) {
        Optional.ofNullable(get(key)).ifPresent(x -> x.setSeed(randomSeed()));
    }

    private static long randomSeed() {
        return RANDOM.nextLong();
    }

    public static void saveConfig() {
        if (SERVER == null) return;
        try {
            FileUtils.write(SERVER.getSavePath(RESOURCE_WORLD_DATA).toFile(), CODEC.encodeStart(JsonOps.INSTANCE, DATA).resultOrPartial(ResourceWorld.LOGGER::error).orElseThrow().toString(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            ResourceWorld.LOGGER.error("Failed to create config", ex);
        }
    }
}
