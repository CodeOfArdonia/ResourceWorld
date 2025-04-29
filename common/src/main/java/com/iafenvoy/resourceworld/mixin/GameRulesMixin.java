package com.iafenvoy.resourceworld.mixin;

import com.iafenvoy.resourceworld.config.ResourceGameRules;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRules.class)
public class GameRulesMixin {
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void registerGameRules(CallbackInfo ci) {
        ResourceGameRules.init();
    }
}
