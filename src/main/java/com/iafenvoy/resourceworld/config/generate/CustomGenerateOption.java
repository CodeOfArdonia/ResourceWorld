package com.iafenvoy.resourceworld.config.generate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iafenvoy.resourceworld.util.RLUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
//? >=1.20.5 {
import com.mojang.serialization.MapCodec;
//?}
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.dimension.LevelStem;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public record CustomGenerateOption(JsonObject stem) implements GenerateOption {
    // Keep registry references as JSON until world creation, including during config loading.
    private static final Codec<JsonObject> STEM_CODEC = Codec.PASSTHROUGH.comapFlatMap(dynamic -> {
        JsonElement json = dynamic.convert(JsonOps.INSTANCE).getValue();
        return json.isJsonObject() ? DataResult.success(json.getAsJsonObject()) : DataResult.error(() -> "Expected a LevelStem JSON object");
    }, json -> new Dynamic<>(JsonOps.INSTANCE, json));
    public static final /*? >=1.20.5 {*/MapCodec/*?} else {*//*Codec*//*?}*/<CustomGenerateOption> CODEC = RecordCodecBuilder./*? >=1.20.5 {*/mapCodec/*?} else {*//*create*//*?}*/(i -> i.group(
            STEM_CODEC.fieldOf("stem").forGetter(CustomGenerateOption::stem)
    ).apply(i, CustomGenerateOption::new));

    public CustomGenerateOption {
        stem = stem.deepCopy();
    }

    @Override
    public JsonObject stem() {
        return this.stem.deepCopy();
    }

    public static CustomGenerateOption fromStem(RegistryAccess registries, LevelStem stem) {
        DataResult<JsonElement> result = LevelStem.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, registries), stem);
        JsonElement json = result.result().orElseThrow(() -> new IllegalArgumentException(result.error().orElseThrow().message()));
        return new CustomGenerateOption(json.getAsJsonObject());
    }

    public static CustomGenerateOption fromFile(MinecraftServer server, String preset) throws IOException {
        return fromFile(getPresetDirectory(server), preset);
    }

    static CustomGenerateOption fromFile(Path directory, String preset) throws IOException {
        if (!preset.matches("[A-Za-z0-9_-]+"))
            throw new IllegalArgumentException("Preset names may only contain letters, numbers, underscores and hyphens");
        Path file = directory.resolve(preset + ".json");
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement json = JsonParser.parseReader(reader);
            if (!json.isJsonObject()) throw new IllegalArgumentException("Expected a LevelStem JSON object");
            return new CustomGenerateOption(json.getAsJsonObject());
        }
    }

    public static List<String> listPresets(MinecraftServer server) {
        return listPresets(getPresetDirectory(server));
    }

    static List<String> listPresets(Path directory) {
        if (!Files.isDirectory(directory)) return List.of();
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.matches("[A-Za-z0-9_-]+\\.json"))
                    .map(name -> name.substring(0, name.length() - 5))
                    .sorted().toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private static Path getPresetDirectory(MinecraftServer server) {
        //? >=1.21 {
        return server.getServerDirectory().resolve("resourceworld/presets");
        //?} else {
        /*return server.getServerDirectory().toPath().resolve("resourceworld/presets");
        *///?}
    }

    @Override
    public /*? >=1.20.5 {*/MapCodec/*?} else {*//*Codec*//*?}*/<CustomGenerateOption> codec() {
        return CODEC;
    }

    @Override
    public LevelStem createStem(RegistryAccess registries) {
        DataResult<LevelStem> result = LevelStem.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, registries), this.stem);
        return result.result().orElseThrow(() -> new IllegalArgumentException(result.error().orElseThrow().message()));
    }

    @Override
    public ResourceLocation getDimensionTypeId() {
        JsonElement type = this.stem.get("type");
        if (type != null && type.isJsonPrimitive() && type.getAsJsonPrimitive().isString()) {
            ResourceLocation id = RLUtil.tryParse(type.getAsString());
            if (id != null) return id;
        }
        return RLUtil.id("custom");
    }
}
