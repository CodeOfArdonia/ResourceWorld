package com.iafenvoy.resourceworld.mixin;

import com.iafenvoy.resourceworld.config.ResourceWorldData;
import com.iafenvoy.resourceworld.config.WorldConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @ModifyExpressionValue(method = "sendWorldInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getWorldBorder()Lnet/minecraft/world/border/WorldBorder;"))
    private WorldBorder handleBorderPacketTarget(WorldBorder original, @Local(argsOnly = true) ServerWorld world) {
        //I can force redirect all, but I modify resource worlds only for better capability.
        ResourceWorldData config = WorldConfig.get(world.getRegistryKey());
        return config == null ? original : world.getWorldBorder();
    }
}
