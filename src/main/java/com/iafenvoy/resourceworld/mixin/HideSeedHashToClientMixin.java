package com.iafenvoy.resourceworld.mixin;

import com.iafenvoy.resourceworld.config.ResourceWorldData;
import com.iafenvoy.resourceworld.config.WorldConfig;
//? >=1.20.5 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
//?} else {
/*import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
*///?}
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(/*? >=1.20.5 {*/CommonPlayerSpawnInfo/*?} else {*//*ClientboundRespawnPacket*//*?}*/.class)
public class HideSeedHashToClientMixin {
    @Shadow
    @Final
    private ResourceKey<Level> dimension;

    @Mutable
    @Shadow
    @Final
    private long seed;

    @Inject(method = "write", at = @At("HEAD"))
    private void injectEmptyHashedSeed(/*? >=1.20.5 {*/RegistryFriendlyByteBuf/*?} else {*//*FriendlyByteBuf*//*?}*/ buf, CallbackInfo ci) {
        ResourceWorldData data = WorldConfig.get(this.dimension);
        if (data != null && data.getSettings().isHideSeedHash()) this.seed = 0;
    }
}
