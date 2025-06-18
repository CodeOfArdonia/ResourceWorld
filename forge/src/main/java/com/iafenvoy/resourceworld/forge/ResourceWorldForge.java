package com.iafenvoy.resourceworld.forge;

import com.iafenvoy.resourceworld.MixinCache;
import com.iafenvoy.resourceworld.ResourceWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Mod;

@Mod(ResourceWorld.MOD_ID)
public final class ResourceWorldForge {
    public ResourceWorldForge() {
        MixinCache.WORLD_CHANGE_CALLBACKS.add(MinecraftServer::markWorldsDirty);
    }
}
