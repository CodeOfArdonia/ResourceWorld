package com.iafenvoy.resourceworld.mixin;

import com.iafenvoy.resourceworld.util.ServerPlayerEntityAccessor;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements ServerPlayerEntityAccessor {
    @Unique
    private String resource_world$language = "en_us";

    @Inject(method = "setClientSettings", at = @At("RETURN"))
    private void cachePlayerLanguage(ClientSettingsC2SPacket packet, CallbackInfo ci) {
        this.resource_world$language = packet.language();
    }

    @Override
    public String resource_world$getLanguage() {
        return this.resource_world$language;
    }
}
