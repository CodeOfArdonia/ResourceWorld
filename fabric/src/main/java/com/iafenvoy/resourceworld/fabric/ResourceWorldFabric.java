package com.iafenvoy.resourceworld.fabric;

import net.fabricmc.api.ModInitializer;

public final class ResourceWorldFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricEntryPointLoader.init();
    }
}
