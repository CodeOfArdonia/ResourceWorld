package com.iafenvoy.resourceworld.util;

import com.mojang.serialization.Codec;
import net.minecraft.world.GameRules;

import java.util.LinkedHashMap;
import java.util.Map;

public final class GameRuleCodec {
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
}
