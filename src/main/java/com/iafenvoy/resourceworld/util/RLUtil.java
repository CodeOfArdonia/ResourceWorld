package com.iafenvoy.resourceworld.util;

import com.iafenvoy.resourceworld.ResourceWorld;
import net.minecraft.resources.Identifier;

public final class RLUtil {
    //? forge {
    /*@SuppressWarnings("removal")
     *///?}
    public static Identifier id(String id) {
        return Identifier.fromNamespaceAndPath(ResourceWorld.MOD_ID, id);
    }

    //? forge {
    /*@SuppressWarnings("removal")
     *///?}
    public static Identifier id(String namespace, String id) {
        return Identifier.fromNamespaceAndPath(namespace, id);
    }

    //? forge {
    /*@SuppressWarnings("removal")
     *///?}
    public static Identifier tryParse(String id) {
        try {
            return Identifier.parse(id);
        } catch (Exception e) {
            return null;
        }
    }
}
