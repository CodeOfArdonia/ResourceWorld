package com.iafenvoy.resourceworld.forge;

import com.iafenvoy.resourceworld.ResourceWorld;
import com.iafenvoy.resourceworld.data.EntryPointLoader;
import com.iafenvoy.resourceworld.data.RandomTeleportEntrypoint;
import com.iafenvoy.resourceworld.data.RandomTeleportProvider;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ForgeEntryPointLoader extends EntryPointLoader {
    public static void init() {
        INSTANCE = new ForgeEntryPointLoader();
    }

    private static <T> List<T> combine(List<T> l1, List<T> l2) {
        l1.addAll(l2);
        return l1;
    }

    @Override
    protected List<RandomTeleportEntrypoint> loadEntries() {
        final Type type = Type.getType(RandomTeleportProvider.class);
        return ModList.get()
                .getAllScanData()
                .stream()
                .map(scanData -> scanData.getAnnotations()
                        .stream()
                        .filter(x -> x.annotationType().equals(type))
                        .map(ModFileScanData.AnnotationData::memberName)
                        .map(ForgeEntryPointLoader::getClassUnsafely)
                        .filter(Objects::nonNull)
                        .map(ForgeEntryPointLoader::constructUnsafely)
                        .filter(x -> x instanceof RandomTeleportEntrypoint)
                        .map(RandomTeleportEntrypoint.class::cast)
                        .toList())
                .reduce(new ArrayList<>(), ForgeEntryPointLoader::combine);
    }

    @Nullable
    public static Class<?> getClassUnsafely(String name) {
        try {
            return Class.forName(name);
        } catch (ReflectiveOperationException e) {
            ResourceWorld.LOGGER.error("Failed to get class", e);
            return null;
        }
    }

    @Nullable
    public static <V> V constructUnsafely(Class<V> cls) {
        try {
            Constructor<V> constructor = cls.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            ResourceWorld.LOGGER.error("Failed to construct object");
            return null;
        }
    }
}
