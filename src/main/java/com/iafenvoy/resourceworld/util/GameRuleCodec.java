package com.iafenvoy.resourceworld.util;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.flag.FeatureFlags;

public final class GameRuleCodec {
    public static final Codec<GameRules> CODEC = GameRules.codec(FeatureFlags.DEFAULT_FLAGS);
}
