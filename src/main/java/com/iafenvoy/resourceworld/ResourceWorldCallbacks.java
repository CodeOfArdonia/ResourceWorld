package com.iafenvoy.resourceworld;

import net.minecraft.server.MinecraftServer;

import java.util.function.Consumer;

public final class ResourceWorldCallbacks {
    private static Consumer<MinecraftServer> markWorldDirty = _ -> {
    };

    private ResourceWorldCallbacks() {
    }

    public static void registerMarkWorldDirty(Consumer<MinecraftServer> callback) {
        markWorldDirty = callback;
    }

    public static void markWorldDirty(MinecraftServer server) {
        markWorldDirty.accept(server);
    }
}
