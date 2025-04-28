package com.iafenvoy.resourceworld.forge;

import com.iafenvoy.resourceworld.ResourceWorld;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ResourceWorld.MOD_ID)
public final class ExampleModForge {
    @SuppressWarnings("removal")
    public ExampleModForge() {
        EventBuses.registerModEventBus(ResourceWorld.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        ResourceWorld.init();
    }
}
