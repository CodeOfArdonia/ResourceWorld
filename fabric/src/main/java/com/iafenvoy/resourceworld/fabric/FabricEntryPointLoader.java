package com.iafenvoy.resourceworld.fabric;

import com.iafenvoy.resourceworld.data.EntryPointLoader;
import com.iafenvoy.resourceworld.data.RandomTeleportEntrypoint;
import net.fabricmc.loader.api.FabricLoader;

import java.util.List;

public class FabricEntryPointLoader extends EntryPointLoader {
    public static void init() {
        INSTANCE = new FabricEntryPointLoader();
    }

    @Override
    protected List<RandomTeleportEntrypoint> loadEntries() {
        return FabricLoader.getInstance().getEntrypoints("random_teleport_locator", RandomTeleportEntrypoint.class);
    }
}
