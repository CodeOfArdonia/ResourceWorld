package com.iafenvoy.resourceworld.config;

import com.iafenvoy.resourceworld.ResourceWorld;
import net.minecraft.world.GameRules;

public final class ResourceGameRules {
    public static final GameRules.Key<GameRules.IntRule> COOLDOWN_SECOND = GameRules.register("%s:tp_cooldown_seconds".formatted(ResourceWorld.MOD_ID), GameRules.Category.PLAYER, GameRules.IntRule.create(30));
    public static final GameRules.Key<GameRules.BooleanRule> HIDE_SEED_HASH = GameRules.register("%s:hide_seed_hash".formatted(ResourceWorld.MOD_ID), GameRules.Category.PLAYER, GameRules.BooleanRule.create(false));

    public static void init() {
    }
}
