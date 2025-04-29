package com.iafenvoy.resourceworld.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ResourceWorldData {
    public static final Codec<ResourceWorldData> CODEC = RecordCodecBuilder.create(i -> i.group(
            SingleWorldData.CODEC.fieldOf("world").forGetter(ResourceWorldData::getWorld),
            SingleWorldData.CODEC.fieldOf("nether").forGetter(ResourceWorldData::getNether),
            SingleWorldData.CODEC.fieldOf("end").forGetter(ResourceWorldData::getEnd)
    ).apply(i, ResourceWorldData::new));
    private final SingleWorldData world, nether, end;

    public ResourceWorldData() {
        this(new SingleWorldData(), new SingleWorldData(), new SingleWorldData());
    }

    public ResourceWorldData(SingleWorldData world, SingleWorldData nether, SingleWorldData end) {
        this.world = world;
        this.nether = nether;
        this.end = end;
    }

    public SingleWorldData getWorld() {
        return this.world;
    }

    public SingleWorldData getNether() {
        return this.nether;
    }

    public SingleWorldData getEnd() {
        return this.end;
    }
}
