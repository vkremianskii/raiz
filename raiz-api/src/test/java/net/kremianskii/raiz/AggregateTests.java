package net.kremianskii.raiz;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AggregateTests {
    @Test
    void isNotNewAndIsNotDirtyWhenVersionIsEqualToSavedVersion() {
        var subj = new TestAggregate(
                new TestAggregateId(1),
                new AggregateVersion<>(1, List.of(), 1));
        assertFalse(subj.isNew());
        assertFalse(subj.isDirty());
    }

    @Test
    void isNewAndIsDirtyWhenSavedVersionIsNull() {
        var subj = new TestAggregate(
                new TestAggregateId(1),
                new AggregateVersion<>(1, List.of(), null));
        assertTrue(subj.isNew());
        assertTrue(subj.isDirty());
    }

    @Test
    void isNotNewAndIsDirtyWhenVersionIsGreaterThanSavedVersion() {
        var subj = new TestAggregate(
                new TestAggregateId(1),
                new AggregateVersion<>(2, List.of(), 1));
        assertFalse(subj.isNew());
        assertTrue(subj.isDirty());
    }

    private static final class TestAggregate extends Aggregate<TestAggregateId, TestAggregateEvent> {
        TestAggregate(TestAggregateId id, AggregateVersion<TestAggregateId, TestAggregateEvent> version) {
            super(id, version);
        }
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
