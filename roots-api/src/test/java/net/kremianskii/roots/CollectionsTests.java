package net.kremianskii.roots;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static net.kremianskii.roots.util.Collections.firstOrThrow;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollectionsTests {
    @Test
    void firstOrThrowThrowsWhenNoSuchElement() {
        var collection = List.of("a");
        assertThatThrownBy(() -> firstOrThrow(collection, item -> item.equals("b")))
                .isInstanceOf(NoSuchElementException.class);
    }
}
