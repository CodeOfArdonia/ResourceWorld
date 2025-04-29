package com.iafenvoy.resourceworld.mixin;

import com.iafenvoy.resourceworld.data.WorldConfig;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onServerStart(CallbackInfo ci) {
        WorldConfig.bootstrap((MinecraftServer) (Object) this);
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void onServerStop(CallbackInfo ci) {
        WorldConfig.stop();
    }
}
