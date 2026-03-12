package com.iafenvoy.resourceworld.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ObjectUtil {
    @NotNull
    public static <O, T extends Throwable> O nonNullOrThrow(@Nullable O obj, Supplier<T> supplier) throws T {
        if (obj == null) throw supplier.get();
        return obj;
    }

    public static <T extends Throwable> void assertOrThrow(boolean b, Supplier<T> supplier) throws T {
        if (!b) throw supplier.get();
    }
}
