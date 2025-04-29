package com.iafenvoy.resourceworld.mixin;

import com.google.common.collect.ImmutableList;
import com.iafenvoy.resourceworld.config.WorldConfig;
import com.iafenvoy.resourceworld.util.MinecraftServerAccessor;
import com.iafenvoy.resourceworld.util.Timeout;
import net.minecraft.client.gui.WorldGenerationProgressTracker;
import net.minecraft.registry.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.UnmodifiableLevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.Executor;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin extends ThreadExecutor<ServerTask> implements MinecraftServerAccessor {
    @Shadow
    @Final
    private Map<RegistryKey<World>, ServerWorld> worlds;

    @Shadow
    @Final
    private Executor workerExecutor;

    @Shadow
    @Final
    protected LevelStorage.Session session;

    @Shadow
    @Final
    protected SaveProperties saveProperties;

    @Shadow
    @Final
    private CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries;

    protected MinecraftServerMixin(String name) {
        super(name);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onServerStart(CallbackInfo ci) {
        WorldConfig.bootstrap((MinecraftServer) (Object) this);
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void onServerStop(CallbackInfo ci) {
        WorldConfig.stop();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        Timeout.runTimeout((MinecraftServer) (Object) this);
    }

    @Override
    public void resource_world$recreateWorld(RegistryKey<World> key) {
        ServerWorldProperties serverWorldProperties = this.saveProperties.getMainWorldProperties();
        boolean bl = this.saveProperties.isDebugWorld();
        Registry<DimensionOptions> registry = this.combinedDynamicRegistries.getCombinedRegistryManager().get(RegistryKeys.DIMENSION);
        GeneratorOptions generatorOptions = this.saveProperties.getGeneratorOptions();
        long l = generatorOptions.getSeed();
        long m = BiomeAccess.hashSeed(l);
        ServerWorld serverWorld = this.worlds.get(World.OVERWORLD);
        WorldBorder worldBorder = serverWorld.getWorldBorder();
        RandomSequencesState randomSequencesState = serverWorld.getRandomSequences();
        UnmodifiableLevelProperties unmodifiableLevelProperties = new UnmodifiableLevelProperties(this.saveProperties, serverWorldProperties);
        ServerWorld serverWorld2 = new ServerWorld((MinecraftServer) (Object) this, this.workerExecutor, this.session, unmodifiableLevelProperties, key, registry.get(key.getValue()), new WorldGenerationProgressTracker(16), bl, m, ImmutableList.of(), false, randomSequencesState);
        worldBorder.addListener(new WorldBorderListener.WorldBorderSyncer(serverWorld2.getWorldBorder()));
        this.worlds.put(key, serverWorld2);
    }
}
