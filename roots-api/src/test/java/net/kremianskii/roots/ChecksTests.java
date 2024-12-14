package net.kremianskii.roots;

import org.junit.jupiter.api.Test;

import static net.kremianskii.roots.util.Checks.check;
import static net.kremianskii.roots.util.Checks.require;
import static net.kremianskii.roots.util.Checks.requirePositive;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChecksTests {
    @Test
    void requireThrowsWhenConditionIsNotSatisfied() {
        assertThatThrownBy(() -> require(false, () -> "")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requirePositiveThrowsWhenValueIsNotPositive() {
        assertThatThrownBy(() -> requirePositive("value", 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> requirePositive("value", -1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requireNonNegativeThrowsWhenValueIsNegative() {
        assertThatThrownBy(() -> requirePositive("value", -1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void checkThrowsWhenConditionIsNotSatisfied() {
        assertThatThrownBy(() -> check(false, () -> "")).isInstanceOf(IllegalStateException.class);
    }
}
