package com.iafenvoy.resourceworld._loader.neoforge;

import com.iafenvoy.resourceworld.ResourceWorld;
import com.iafenvoy.resourceworld.ResourceWorldCallbacks;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.common.Mod;

@Mod(ResourceWorld.MOD_ID)
public final class ResourceWorldNeoForge {
    public ResourceWorldNeoForge() {
        ResourceWorldCallbacks.registerMarkWorldDirty(MinecraftServer::markWorldsDirty);
    }
}
