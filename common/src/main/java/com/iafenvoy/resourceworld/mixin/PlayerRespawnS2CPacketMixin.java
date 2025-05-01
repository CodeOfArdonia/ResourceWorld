package com.iafenvoy.resourceworld.mixin;

import com.iafenvoy.resourceworld.config.WorldConfig;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRespawnS2CPacket.class)
public class PlayerRespawnS2CPacketMixin {
    @Shadow
    @Final
    private RegistryKey<World> dimension;

    @Mutable
    @Shadow
    @Final
    private long sha256Seed;

    @Inject(method = "write", at = @At("HEAD"))
    private void injectEmptyHashedSeed(PacketByteBuf buf, CallbackInfo ci) {
        if (WorldConfig.get(this.dimension) != null && WorldConfig.shouldHideSeed()) this.sha256Seed = 0;
    }
}
