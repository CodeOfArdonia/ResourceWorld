package com.iafenvoy.resourceworld.mixin;

import com.iafenvoy.resourceworld.util.ThreadedAnvilChunkStorageAccessor;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.*;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Queue;
import java.util.function.BooleanSupplier;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class ThreadedAnvilChunkStorageMixin implements ThreadedAnvilChunkStorageAccessor {
    @Shadow
    @Final
    ServerWorld world;

    @Shadow
    private ChunkGenerator chunkGenerator;

    @Mutable
    @Shadow
    @Final
    private NoiseConfig noiseConfig;

    @Mutable
    @Shadow
    @Final
    private StructurePlacementCalculator structurePlacementCalculator;

    @Shadow
    @Final
    private Long2ObjectLinkedOpenHashMap<ChunkHolder> currentChunkHolders;

    @Shadow
    private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkHolders;

    @Shadow
    @Final
    private Long2ObjectLinkedOpenHashMap<ChunkHolder> chunksToUnload;

    @Shadow
    @Final
    private LongSet loadedChunks;

    @Shadow
    @Final
    LongSet unloadedChunks;

    @Shadow
    @Final
    private Long2ByteMap chunkToType;

    @Shadow
    @Final
    private Long2LongMap chunkToNextSaveTimeMs;

    @Shadow
    @Final
    private Queue<Runnable> unloadTaskQueue;

    @Shadow
    public abstract ChunkTicketManager getTicketManager();

    @Shadow
    protected abstract void unloadChunks(BooleanSupplier shouldKeepTicking);

    @Shadow
    @Final
    private ServerLightingProvider lightingProvider;

    @Override
    public void resource_world$recreateGenerationSettings() {
        DynamicRegistryManager dynamicRegistryManager = this.world.getRegistryManager();
        long l = this.world.getSeed();
        if (this.chunkGenerator instanceof NoiseChunkGenerator noiseChunkGenerator)
            this.noiseConfig = NoiseConfig.create(noiseChunkGenerator.getSettings().value(), dynamicRegistryManager.getWrapperOrThrow(RegistryKeys.NOISE_PARAMETERS), l);
        else
            this.noiseConfig = NoiseConfig.create(ChunkGeneratorSettings.createMissingSettings(), dynamicRegistryManager.getWrapperOrThrow(RegistryKeys.NOISE_PARAMETERS), l);
        this.structurePlacementCalculator = this.chunkGenerator.createStructurePlacementCalculator(dynamicRegistryManager.getWrapperOrThrow(RegistryKeys.STRUCTURE_SET), this.noiseConfig, l);
    }

    @Override
    public void resource_world$clearChunks() {
        this.getTicketManager().tick((ThreadedAnvilChunkStorage) (Object) this);
        this.unloadChunks(() -> true);
        this.lightingProvider.tick();
        this.currentChunkHolders.clear();
        this.chunkHolders.clear();
        this.chunksToUnload.clear();
        this.loadedChunks.clear();
        this.unloadedChunks.clear();
        this.chunkToType.clear();
        this.chunkToNextSaveTimeMs.clear();
        this.unloadTaskQueue.clear();
    }
}
