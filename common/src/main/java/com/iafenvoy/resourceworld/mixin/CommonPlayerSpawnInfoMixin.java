package com.iafenvoy.resourceworld.mixin;

import com.iafenvoy.resourceworld.config.ResourceWorldData;
import com.iafenvoy.resourceworld.config.WorldConfig;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.packet.s2c.play.CommonPlayerSpawnInfo;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommonPlayerSpawnInfo.class)
public class CommonPlayerSpawnInfoMixin {
    @Shadow
    @Final
    private RegistryKey<World> dimension;

    @Mutable
    @Shadow
    @Final
    private long seed;

    @Inject(method = "write", at = @At("HEAD"))
    private void injectEmptyHashedSeed(RegistryByteBuf buf, CallbackInfo ci) {
        ResourceWorldData data = WorldConfig.get(this.dimension);
        if (data != null && data.getSettings().isHideSeedHash()) this.seed = 0;
    }
}
