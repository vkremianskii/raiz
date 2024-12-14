package net.kremianskii.roots;

import org.junit.jupiter.api.Test;

import java.util.List;

import static net.kremianskii.roots.AggregateVersion.newVersion;
import static net.kremianskii.roots.AggregateVersion.savedVersion;
import static org.assertj.core.api.Assertions.assertThat;

class AggregateVersionTests {
    @Test
    void advancesNewVersion() {
        // given
        var aggregateId = new TestAggregateId(1);
        var event1 = new TestAggregateEvent(aggregateId);
        var event2 = new TestAggregateEvent(aggregateId);
        AggregateVersion<TestAggregateId, TestAggregateEvent> version = newVersion(event1);

        // when
        var advanced = version.advance(List.of(event2));

        // then
        assertThat(advanced).isEqualTo(new AggregateVersion<>(2, List.of(event1, event2), null));
    }

    @Test
    void advancesSavedVersion() {
        // given
        var event = new TestAggregateEvent(new TestAggregateId(1));
        AggregateVersion<TestAggregateId, TestAggregateEvent> version = savedVersion(1);

        // when
        var advanced = version.advance(List.of(event));

        // then
        assertThat(advanced).isEqualTo(new AggregateVersion<>(2, List.of(event), 1));
    }

    private static final class TestAggregateId extends AggregateId<Long> {
        TestAggregateId(long value) {
            super(value);
        }
    }

    private static final class TestAggregateEvent extends AggregateEvent<TestAggregateId> {
        TestAggregateEvent(TestAggregateId aggregateId) {
            super(aggregateId);
        }
    }
}
