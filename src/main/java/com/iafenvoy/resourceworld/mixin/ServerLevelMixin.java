package com.iafenvoy.resourceworld.mixin;

import com.iafenvoy.resourceworld.MixinCache;
import com.iafenvoy.resourceworld.ResourceWorld;
import com.iafenvoy.resourceworld.config.WorldConfig;
import com.iafenvoy.resourceworld.data.ResourceWorldHelper;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {
    protected ServerLevelMixin(WritableLevelData properties, ResourceKey<Level> registryRef, RegistryAccess registryManager, Holder<DimensionType> dimensionEntry, Supplier<ProfilerFiller> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/WorldOptions;seed()J"))
    private long handleResourceWorldCreatingSeeds(long original) {
        long seed = WorldConfig.getSeed(this.dimension());
        return seed != 0 ? seed : original;
    }

    @Inject(method = "getSeed", at = @At("HEAD"), cancellable = true)
    private void handleResourceWorldSeeds(CallbackInfoReturnable<Long> cir) {
        long seed = WorldConfig.getSeed(this.dimension());
        if (seed != 0) cir.setReturnValue(seed);
    }

    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    private void cancelSaveWhenReset(CallbackInfo ci) {
        if (ResourceWorldHelper.RESETTING.contains(this.dimension())) {
            ResourceWorld.LOGGER.warn("Cancelled saving due to resource world reset");
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void beforeTick(CallbackInfo ci) {
        if (ResourceWorldHelper.RESETTING.contains(this.dimension())) ci.cancel();
        else if (!this.isClientSide) MixinCache.CURRENT_TICKING_WORLD = this.dimension();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void afterTick(CallbackInfo ci) {
        MixinCache.CURRENT_TICKING_WORLD = null;
    }

    @Override
    public @NotNull GameRules getGameRules() {
        return WorldConfig.getGameRules(this.dimension()).orElse(super.getGameRules());
    }

    @Override
    public @NotNull Difficulty getDifficulty() {
        return WorldConfig.getDifficulty(this.dimension()).orElse(super.getDifficulty());
    }
}
