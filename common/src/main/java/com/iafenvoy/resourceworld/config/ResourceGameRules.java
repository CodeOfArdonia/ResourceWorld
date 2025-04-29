package com.iafenvoy.resourceworld.config;

import net.minecraft.world.GameRules;

public final class ResourceGameRules {
    public static final GameRules.Key<GameRules.IntRule> COOLDOWN_SECOND = GameRules.register("%s:tp_cooldown_seconds", GameRules.Category.PLAYER, GameRules.IntRule.create(30));

    public static void init() {
    }
}
