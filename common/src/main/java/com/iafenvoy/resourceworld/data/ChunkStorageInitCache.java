package com.iafenvoy.resourceworld.data;

import com.mojang.datafixers.DataFixer;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.level.storage.LevelStorage;

import java.util.HashMap;
import java.util.Map;

public class ChunkStorageInitCache {
    public static final Map<RegistryKey<World>, Holder> DATA = new HashMap<>();

    public record Holder(LevelStorage.Session session, DataFixer dataFixer) {
    }
}
