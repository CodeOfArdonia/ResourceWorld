package com.iafenvoy.resourceworld.config.generate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iafenvoy.resourceworld.config.ResourceWorldData;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
//? >=1.20.5 {
import net.minecraft.core.RegistrationInfo;
//?}
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomGenerateOptionTest {
    private static final String PRESET = """
            {
              "type": "minecraft:overworld",
              "generator": {
                "type": "minecraft:noise",
                "settings": "minecraft:overworld",
                "biome_source": {"type": "minecraft:fixed", "biome": "minecraft:desert"}
              }
            }
            """;
    private static RegistryAccess registries;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        HolderLookup.Provider lookup = VanillaRegistries.createLookup();
        // Deliberately omit LEVEL_STEM: a custom world must not need a dimension registry entry.
        registries = new RegistryAccess.ImmutableRegistryAccess(List.of(
                copyRegistry(Registries.DIMENSION_TYPE, lookup.lookupOrThrow(Registries.DIMENSION_TYPE)),
                copyRegistry(Registries.BIOME, lookup.lookupOrThrow(Registries.BIOME)),
                copyRegistry(Registries.NOISE_SETTINGS, lookup.lookupOrThrow(Registries.NOISE_SETTINGS))
        ));
    }

    private static <T> Registry<T> copyRegistry(ResourceKey<Registry<T>> key, HolderLookup.RegistryLookup<T> lookup) {
        MappedRegistry<T> registry = new MappedRegistry<>(key, Lifecycle.stable());
        lookup.listElements().forEach(holder -> registry.register(holder.unwrapKey().orElseThrow(), holder.value(),
                /*? >=1.20.5 {*/RegistrationInfo.BUILT_IN/*?} else {*//*Lifecycle.stable()*//*?}*/));
        return registry.freeze();
    }

    @Test
    void createsUnregisteredNoiseDimensionAndSnapshotsDirectStem() {
        CustomGenerateOption option = new CustomGenerateOption(JsonParser.parseString(PRESET).getAsJsonObject());
        LevelStem stem = option.createStem(registries);
        assertInstanceOf(NoiseBasedChunkGenerator.class, stem.generator());
        assertTrue(stem.generator().getBiomeSource().possibleBiomes().stream().allMatch(biome -> biome.is(Biomes.DESERT)));
        CustomGenerateOption snapshot = CustomGenerateOption.fromStem(registries, stem);
        assertEquals(option.stem(), snapshot.stem());
    }

    @Test
    void serializesWithoutRegistryContextAndDoesNotDependOnSourceFile(@TempDir Path directory) throws IOException {
        Path file = directory.resolve("desert.json");
        Files.writeString(file, PRESET);
        CustomGenerateOption option = CustomGenerateOption.fromFile(directory, "desert");
        JsonElement saved = GenerateOption.CODEC.encodeStart(JsonOps.INSTANCE, option).result().orElseThrow();
        assertEquals("resource_world:custom", saved.getAsJsonObject().get("type").getAsString());
        Files.delete(file);
        GenerateOption restored = GenerateOption.CODEC.parse(JsonOps.INSTANCE, saved).result().orElseThrow();
        assertInstanceOf(CustomGenerateOption.class, restored);
        assertEquals(option.stem(), ((CustomGenerateOption) restored).stem());
        assertNotNull(restored.createStem(registries));
        assertNotSame(restored.createStem(registries).generator(), restored.createStem(registries).generator());
    }

    @Test
    void preservesCustomGenerationAndSeedInWorldConfig() {
        CustomGenerateOption option = new CustomGenerateOption(JsonParser.parseString(PRESET).getAsJsonObject());
        ResourceWorldData data = new ResourceWorldData(option);
        data.setSeed(12345L);
        JsonElement saved = ResourceWorldData.CODEC.encodeStart(JsonOps.INSTANCE, data).result().orElseThrow();
        //? !neoforge {
        // Game rule decoding needs loader mixins; this test covers generation and seed persistence.
        saved.getAsJsonObject().remove("gamerules");
        //?}
        ResourceWorldData restored = ResourceWorldData.CODEC.parse(JsonOps.INSTANCE, saved).result().orElseThrow();
        assertEquals(12345L, restored.getSeed());
        assertEquals(option.stem(), ((CustomGenerateOption) restored.getGenerateOption()).stem());
        assertNotNull(restored.getGenerateOption().createStem(registries));
    }

    @Test
    void existingGenerationTypesStillRoundTrip() {
        for (String definition : List.of(
                "{\"type\":\"resource_world:type\",\"target\":\"minecraft:overworld\"}",
                "{\"type\":\"resource_world:flat\",\"preset\":\"minecraft:bedrock\"}",
                "{\"type\":\"resource_world:mirror\",\"dimension\":\"minecraft:the_nether\"}",
                "{\"type\":\"resource_world:template\",\"template\":\"example\",\"dimension\":\"minecraft:overworld\"}")) {
            JsonElement json = JsonParser.parseString(definition);
            GenerateOption restored = GenerateOption.CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow();
            assertEquals(json, GenerateOption.CODEC.encodeStart(JsonOps.INSTANCE, restored).result().orElseThrow());
        }
    }

    @Test
    void rejectsUnknownRegistryReferences() {
        JsonObject json = JsonParser.parseString(PRESET).getAsJsonObject();
        json.getAsJsonObject("generator").addProperty("settings", "resource_world:missing_settings");
        CustomGenerateOption missingSettings = new CustomGenerateOption(json);
        assertThrows(IllegalArgumentException.class, () -> missingSettings.createStem(registries));
        json = JsonParser.parseString(PRESET).getAsJsonObject();
        json.addProperty("type", "resource_world:missing_type");
        CustomGenerateOption option = new CustomGenerateOption(json);
        assertThrows(IllegalArgumentException.class, () -> option.createStem(registries));
    }

    @Test
    void snapshotsCannotBeMutatedThroughInputOrAccessor() {
        JsonObject json = JsonParser.parseString(PRESET).getAsJsonObject();
        CustomGenerateOption option = new CustomGenerateOption(json);
        json.remove("generator");
        option.stem().remove("generator");
        assertNotNull(option.createStem(registries));
    }

    @Test
    void rejectsInvalidFilesAndTraversalAndSuggestsOnlyNamedJsonFiles(@TempDir Path directory) throws IOException {
        Files.writeString(directory.resolve("desert.json"), PRESET);
        Files.writeString(directory.resolve("broken.json"), "{");
        Files.writeString(directory.resolve("array.json"), "[]");
        Files.writeString(directory.resolve("notes.txt"), "notes");
        Files.createDirectory(directory.resolve("folder.json"));
        assertEquals(List.of("array", "broken", "desert"), CustomGenerateOption.listPresets(directory));
        assertThrows(IOException.class, () -> CustomGenerateOption.fromFile(directory, "missing"));
        assertThrows(RuntimeException.class, () -> CustomGenerateOption.fromFile(directory, "broken"));
        assertThrows(IllegalArgumentException.class, () -> CustomGenerateOption.fromFile(directory, "array"));
        for (String name : List.of("../desert", "..\\desert", "E:\\desert", "desert.json"))
            assertThrows(IllegalArgumentException.class, () -> CustomGenerateOption.fromFile(directory, name));
        assertTrue(CustomGenerateOption.listPresets(directory.resolve("missing")).isEmpty());
    }
}
