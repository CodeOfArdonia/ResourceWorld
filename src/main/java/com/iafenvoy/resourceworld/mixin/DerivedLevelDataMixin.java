package com.iafenvoy.resourceworld.mixin;

import com.iafenvoy.resourceworld.MixinCache;
import com.iafenvoy.resourceworld.config.WorldConfig;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DerivedLevelData.class)
public class DerivedLevelDataMixin {
    @ModifyReturnValue(method = "getDifficulty", at = @At("RETURN"))
    private Difficulty useResourceWorldDifficulty(Difficulty original) {
        ResourceKey<Level> key = MixinCache.getResourceWorldKey((ServerLevelData) this);
        return key == null ? original : java.util.Optional.ofNullable(WorldConfig.get(key)).map(x -> x.getDifficulty()).orElse(original);
    }
}
