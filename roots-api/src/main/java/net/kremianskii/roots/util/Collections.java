package net.kremianskii.roots.util;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public final class Collections {
    private Collections() {
    }

    public static <T> T firstOrThrow(Collection<T> collection, Predicate<T> predicate) {
        return collection.stream().filter(predicate).findFirst()
                .orElseThrow(NoSuchElementException::new);
    }
}
