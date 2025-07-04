package com.iafenvoy.resourceworld.mixin;

import com.iafenvoy.resourceworld.MixinCache;
import com.iafenvoy.resourceworld.ResourceWorld;
import com.iafenvoy.resourceworld.config.WorldConfig;
import com.iafenvoy.resourceworld.data.ResourceWorldHelper;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Inject(method = "getSeed", at = @At("HEAD"), cancellable = true)
    private void handleResourceWorldSeeds(CallbackInfoReturnable<Long> cir) {
        long seed = WorldConfig.getSeed(this.getRegistryKey());
        if (seed != 0) cir.setReturnValue(seed);
    }

    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    private void cancelSaveWhenReset(CallbackInfo ci) {
        if (ResourceWorldHelper.RESETTING.contains(this.getRegistryKey())) {
            ResourceWorld.LOGGER.warn("Cancelled saving due to resource world reset");
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void beforeTick(CallbackInfo ci) {
        if (ResourceWorldHelper.RESETTING.contains(this.getRegistryKey())) ci.cancel();
        else if (!this.isClient) MixinCache.CURRENT_TICKING_WORLD = this.getRegistryKey();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void afterTick(CallbackInfo ci) {
        MixinCache.CURRENT_TICKING_WORLD = null;
    }

    @Override
    public GameRules getGameRules() {
        return WorldConfig.getGameRules(this.getRegistryKey()).orElse(super.getGameRules());
    }

    @Override
    public Difficulty getDifficulty() {
        return WorldConfig.getDifficulty(this.getRegistryKey()).orElse(super.getDifficulty());
    }
}
