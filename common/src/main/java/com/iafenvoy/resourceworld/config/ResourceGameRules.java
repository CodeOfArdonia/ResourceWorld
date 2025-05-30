package com.iafenvoy.resourceworld.config;

import com.iafenvoy.resourceworld.ResourceWorld;
import com.mojang.serialization.Codec;
import net.minecraft.world.GameRules;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ResourceGameRules {
    public static final GameRules.Key<GameRules.IntRule> COOLDOWN_SECOND = GameRules.register("%s:tp_cooldown_seconds".formatted(ResourceWorld.MOD_ID), GameRules.Category.PLAYER, GameRules.IntRule.create(30));
    public static final GameRules.Key<GameRules.BooleanRule> HIDE_SEED_HASH = GameRules.register("%s:hide_seed_hash".formatted(ResourceWorld.MOD_ID), GameRules.Category.PLAYER, GameRules.BooleanRule.create(false));

    public static final Codec<GameRules> CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING).xmap(m -> {
        GameRules gameRules = new GameRules();
        GameRules.accept(new GameRules.Visitor() {
            @Override
            public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                String name = key.getName();
                if (m.containsKey(name))
                    gameRules.get(key).deserialize(m.get(name));
            }
        });
        return gameRules;
    }, g -> {
        Map<String, String> map = new LinkedHashMap<>();
        GameRules.accept(new GameRules.Visitor() {
            @Override
            public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                map.put(key.getName(), g.get(key).serialize());
            }
        });
        return map;
    });

    public static void init() {
    }
}
