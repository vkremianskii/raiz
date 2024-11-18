package net.kremianskii.raiz;

import org.junit.jupiter.api.Test;

import static net.kremianskii.raiz.util.Checks.check;
import static net.kremianskii.raiz.util.Checks.require;
import static net.kremianskii.raiz.util.Checks.requirePositive;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChecksTests {
    @Test
    void requireThrowsWhenConditionIsNotSatisfied() {
        assertThrows(IllegalArgumentException.class, () -> require(false, () -> ""));
    }

    @Test
    void requirePositiveThrowsWhenValueIsNotPositive() {
        assertThrows(IllegalArgumentException.class, () -> requirePositive("value", 0));
        assertThrows(IllegalArgumentException.class, () -> requirePositive("value", -1));
    }

    @Test
    void requireNonNegativeThrowsWhenValueIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> requirePositive("value", -1));
    }

    @Test
    void checkThrowsWhenConditionIsNotSatisfied() {
        assertThrows(IllegalStateException.class, () -> check(false, () -> ""));
    }
}
