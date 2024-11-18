package net.kremianskii.roots.util;

import java.util.function.Supplier;

public final class Checks {
    private Checks() {
    }

    public static void require(boolean condition, Supplier<String> message) {
        if (!condition) {
            throw new IllegalArgumentException(message.get());
        }
    }

    public static <T extends Number> T requirePositive(String name, T value) {
        if (value.intValue() <= 0) {
            throw new IllegalArgumentException(name + " must be positive, was " + value);
        }
        return value;
    }

    public static <T extends Number> T requireNonNegative(String name, T value) {
        if (value.intValue() < 0) {
            throw new IllegalArgumentException(name + " must not be negative, was " + value);
        }
        return value;
    }

    public static void check(boolean condition, Supplier<String> message) {
        if (!condition) {
            throw new IllegalStateException(message.get());
        }
    }
}
