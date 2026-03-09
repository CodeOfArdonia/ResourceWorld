package com.iafenvoy.resourceworld.util;

import com.iafenvoy.resourceworld.mixin.GameRules$ValueAccessor;
import com.mojang.serialization.Codec;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.NotNull;

public final class GameRuleCodec {
    public static final Codec<GameRules> CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING).xmap(m -> {
        GameRules gameRules = new GameRules();
        GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
            @Override
            public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                String name = key.getId();
                if (m.containsKey(name)) ((GameRules$ValueAccessor) gameRules.getRule(key)).deserialize(m.get(name));
            }
        });
        return gameRules;
    }, g -> {
        Map<String, String> map = new LinkedHashMap<>();
        GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
            @Override
            public <T extends GameRules.Value<T>> void visit(GameRules.@NotNull Key<T> key, GameRules.@NotNull Type<T> type) {
                map.put(key.getId(), g.getRule(key).serialize());
            }
        });
        return map;
    });
}
