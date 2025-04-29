package com.iafenvoy.resourceworld.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class SingleWorldData {
    public static final Codec<SingleWorldData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.fieldOf("enabled").forGetter(SingleWorldData::isEnabled),
            Codec.LONG.fieldOf("seed").forGetter(SingleWorldData::getSeed),
            Codec.INT.fieldOf("centerX").forGetter(SingleWorldData::getCenterX),
            Codec.INT.fieldOf("centerZ").forGetter(SingleWorldData::getCenterZ),
            Codec.INT.fieldOf("range").forGetter(SingleWorldData::getRange)
    ).apply(i, SingleWorldData::new));
    private boolean enabled;
    private long seed;
    private int centerX;
    private int centerZ;
    private int range;

    public SingleWorldData() {
        this(false, 0, 0, 0, 4096);
    }

    public SingleWorldData(boolean enabled, long seed, int centerX, int centerZ, int range) {
        this.enabled = enabled;
        this.seed = seed;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.range = range;
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
