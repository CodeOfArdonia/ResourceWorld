package com.iafenvoy.resourceworld.util;

import com.iafenvoy.resourceworld.ResourceWorld;
import net.minecraft.resources.ResourceLocation;

public final class RLUtil {
    //? forge {
    /*@SuppressWarnings("removal")
     *///?}
    public static ResourceLocation id(String id) {
        //? >=1.21 {
        return ResourceLocation.fromNamespaceAndPath(ResourceWorld.MOD_ID, id);
        //?} else {
        /*return new ResourceLocation(ResourceWorld.MOD_ID, id);
        *///?}
    }

    //? forge {
    /*@SuppressWarnings("removal")
     *///?}
    public static ResourceLocation id(String namespace, String id) {
        //? >=1.21 {
        return ResourceLocation.fromNamespaceAndPath(namespace, id);
        //?} else {
        /*return new ResourceLocation(namespace, id);
        *///?}
    }

    //? forge {
    /*@SuppressWarnings("removal")
     *///?}
    public static ResourceLocation tryParse(String id) {
        try {
            //? >=1.21 {
            return ResourceLocation.parse(id);
            //?} else {
            /*return new ResourceLocation(id);
            *///?}
        } catch (Exception e) {
            return null;
        }
    }
}
