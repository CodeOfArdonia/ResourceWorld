package com.iafenvoy.resourceworld.mixin;

import com.iafenvoy.resourceworld.data.ResourceWorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.server.level.ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(method = "setRespawnPosition", at = @At("HEAD"), cancellable = true)
    private void preventResourceWorldRespawn(net.minecraft.server.level.ServerPlayer.RespawnConfig config, boolean sendMessage, CallbackInfo ci) {
        if (config != null && ResourceWorldHelper.isResourceWorld(config.respawnData().dimension())) ci.cancel();
    }
}
