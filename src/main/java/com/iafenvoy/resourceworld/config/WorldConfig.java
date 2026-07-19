package com.iafenvoy.resourceworld.config;

import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.iafenvoy.resourceworld.MixinCache;
import com.iafenvoy.resourceworld.ResourceWorld;
import com.iafenvoy.resourceworld.config.generate.GenerateOption;
import com.iafenvoy.resourceworld.data.ResourceWorldHelper;
import com.iafenvoy.resourceworld.mixin.LevelResourceAccessor;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
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
    private static final int CONFIG_VERSION = 1;
    private static final Codec<ConfigData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("version").forGetter(ConfigData::version),
            Codec.unboundedMap(Codec.STRING, ResourceWorldData.CODEC).fieldOf("worlds").forGetter(ConfigData::worlds)
    ).apply(i, ConfigData::new));
    private static final LevelResource RESOURCE_WORLD_DATA = LevelResourceAccessor.resourceWorld$newInstance("resource_world.json");
    private static final Random RANDOM = new Random();
    private static final Map<String, ResourceWorldData> DATA = new HashMap<>();

    @ApiStatus.Internal
    public static void bootstrap(MinecraftServer server) {
        DATA.clear();
        try {
            JsonElement config = JsonParser.parseString(FileUtils.readFileToString(server.getWorldPath(RESOURCE_WORLD_DATA).toFile(), StandardCharsets.UTF_8));
            if (needsMigration(config)) {
                config = migrateLegacyConfig(config);
                saveConfig(config);
            }
            DATA.putAll(CODEC.parse(JsonOps.INSTANCE, config).resultOrPartial(ResourceWorld.LOGGER::error).orElseThrow().worlds());
        } catch (FileNotFoundException e) {
            saveConfig();
        } catch (Exception e) {
            ResourceWorld.LOGGER.error("Failed to load config", e);
        }
    }

    @ApiStatus.Internal
    public static void initResourceWorld(BiConsumer<ResourceKey<Level>, GenerateOption> consumer) {
        for (Map.Entry<String, ResourceWorldData> entry : DATA.entrySet())
            consumer.accept(ResourceWorldHelper.toRegistryKey(entry.getKey()), entry.getValue().getGenerateOption());
    }

    public static CompletableFuture<Suggestions> appendSuggestions(SuggestionsBuilder builder) {
        DATA.keySet().forEach(builder::suggest);
        return builder.buildFuture();
    }

    @ApiStatus.Internal
    public static void stop() {
        saveConfig();
    }

    public static ResourceWorldData create(String id, GenerateOption stem) {
        ResourceWorldData data = new ResourceWorldData(stem);
        data.setEnabled(true);
        DATA.put(id, data);
        saveConfig();
        return data;
    }

    @Nullable
    public static ResourceWorldData get(ResourceKey<Level> key) {
        return ResourceWorldHelper.isResourceWorld(key) ? DATA.get(ResourceWorldHelper.resolveId(key)) : null;
    }

    public static void remove(ResourceKey<Level> key) {
        if (!ResourceWorldHelper.isResourceWorld(key)) return;
        DATA.remove(ResourceWorldHelper.resolveId(key));
        saveConfig();
    }

    public static long getSeed(ResourceKey<Level> key) {
        return Optional.ofNullable(get(key)).map(ResourceWorldData::getSeed).orElse(0L);
    }

    public static Optional<GameRules> getGameRules(ResourceKey<Level> key) {
        return Optional.ofNullable(get(key)).map(ResourceWorldData::getGameRules);
    }

    public static Optional<Difficulty> getDifficulty(ResourceKey<Level> key) {
        return Optional.ofNullable(get(key)).map(ResourceWorldData::getDifficulty);
    }

    public static void newSeed(ResourceKey<Level> key) {
        Optional.ofNullable(get(key)).ifPresent(x -> x.setSeed(randomSeed()));
    }

    private static long randomSeed() {
        return RANDOM.nextLong();
    }

    private static boolean needsMigration(JsonElement config) {
        return config.isJsonObject() && !config.getAsJsonObject().has("version");
    }

    private static JsonElement migrateLegacyConfig(JsonElement config) {
        if (!config.isJsonObject()) throw new IllegalStateException("Legacy resource world config must be an object");
        JsonObject worlds = config.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : worlds.entrySet()) {
            if (!entry.getValue().isJsonObject()) continue;
            JsonObject world = entry.getValue().getAsJsonObject();
            if (!world.has("generate") || !world.get("generate").isJsonObject()) continue;
            JsonObject generate = world.getAsJsonObject("generate");
            if (!generate.has("type")) continue;
            String type = generate.get("type").getAsString();
            String migratedType = switch (type) {
                case "resource_world:mirror" -> "resource_world:type";
                case "resource_world:copy_world" -> "resource_world:mirror";
                case "resource_world:copy_template" -> "resource_world:template";
                default -> null;
            };
            if (migratedType != null) {
                generate.addProperty("type", migratedType);
            }
        }
        JsonObject migrated = new JsonObject();
        migrated.addProperty("version", CONFIG_VERSION);
        migrated.add("worlds", worlds);
        return migrated;
    }

    private static void saveConfig(JsonElement config) {
        if (MixinCache.SERVER == null) return;
        try {
            FileUtils.write(MixinCache.SERVER.getWorldPath(RESOURCE_WORLD_DATA).toFile(), config.toString(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            ResourceWorld.LOGGER.error("Failed to migrate config", ex);
        }
    }

    public static void saveConfig() {
        if (MixinCache.SERVER == null) return;
        try {
            ConfigData config = new ConfigData(CONFIG_VERSION, DATA);
            FileUtils.write(MixinCache.SERVER.getWorldPath(RESOURCE_WORLD_DATA).toFile(), CODEC.encodeStart(JsonOps.INSTANCE, config).resultOrPartial(ResourceWorld.LOGGER::error).orElseThrow().toString(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            ResourceWorld.LOGGER.error("Failed to create config", ex);
        }
    }

    private record ConfigData(int version, Map<String, ResourceWorldData> worlds) {
    }
}
