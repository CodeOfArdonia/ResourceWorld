package com.iafenvoy.resourceworld.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ResourceWorldData {
    public static final Codec<ResourceWorldData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("targetWorld").forGetter(ResourceWorldData::getTargetWorld),
            Codec.BOOL.fieldOf("enabled").forGetter(ResourceWorldData::isEnabled),
            Codec.LONG.fieldOf("seed").forGetter(ResourceWorldData::getSeed),
            Codec.INT.fieldOf("centerX").forGetter(ResourceWorldData::getCenterX),
            Codec.INT.fieldOf("centerZ").forGetter(ResourceWorldData::getCenterZ),
            Codec.INT.fieldOf("range").forGetter(ResourceWorldData::getRange)
    ).apply(i, ResourceWorldData::new));
    private final Identifier targetWorld;
    private boolean enabled;
    private long seed;
    private int centerX;
    private int centerZ;
    private int range;

    public ResourceWorldData(Identifier targetWorld) {
        this(targetWorld, false, 0, 0, 0, 4096);
    }

    public ResourceWorldData(Identifier targetWorld, boolean enabled, long seed, int centerX, int centerZ, int range) {
        this.targetWorld = targetWorld;
        this.enabled = enabled;
        this.seed = seed;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.range = range;
    }

    public Identifier getTargetWorld() {
        return this.targetWorld;
    }

    public RegistryKey<World> getTargetWorldKey() {
        return RegistryKey.of(RegistryKeys.WORLD, this.targetWorld);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getSeed() {
        return this.seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public int getCenterX() {
        return this.centerX;
    }

    public void setCenterX(int centerX) {
        this.centerX = centerX;
    }

    public int getCenterZ() {
        return this.centerZ;
    }

    public void setCenterZ(int centerZ) {
        this.centerZ = centerZ;
    }

    public int getRange() {
        return this.range;
    }

    public void setRange(int range) {
        this.range = range;
    }
}
