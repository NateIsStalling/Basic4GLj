package com.basic4gl.language.core.internal;

import java.util.List;

public final class CollectionUtil {
    private CollectionUtil() {}

    /**
     * Shim for migrating legacy java.util.Vector usage to ArrayList;
     * review deprecating and/or removing this method in the future.
     * @param list
     * @param size
     * @param <T>
     */
    public static <T> void resize(List<T> list, int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size must be >= 0");
        }

        int currentSize = list.size();
        if (size < currentSize) {
            list.subList(size, currentSize).clear();
            return;
        }

        for (int i = currentSize; i < size; i++) {
            list.add(null);
        }
    }

    public static <T> T last(List<T> list) {
        return list.get(list.size() - 1);
    }
}
