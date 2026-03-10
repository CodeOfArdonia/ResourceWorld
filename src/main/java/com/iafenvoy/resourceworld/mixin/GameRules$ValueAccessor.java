package com.iafenvoy.resourceworld.mixin;

import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRules.Value.class)
public interface GameRules$ValueAccessor {
    @Invoker("deserialize")
    void resourceWorld$deserialize(String value);
}
