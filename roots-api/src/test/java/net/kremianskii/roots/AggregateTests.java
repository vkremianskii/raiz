package net.kremianskii.roots;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AggregateTests {
    @Test
    void isNotNewAndIsNotDirtyWhenVersionIsEqualToSavedVersion() {
        var subj = new TestAggregate(
                new TestAggregateId(1),
                new AggregateVersion<>(1, List.of(), 1));
        assertThat(subj.isNew()).isFalse();
        assertThat(subj.isDirty()).isFalse();
    }

    @Test
    void isNewAndIsDirtyWhenSavedVersionIsNull() {
        var subj = new TestAggregate(
                new TestAggregateId(1),
                new AggregateVersion<>(1, List.of(), null));
        assertThat(subj.isNew()).isTrue();
        assertThat(subj.isDirty()).isTrue();
    }

    @Test
    void isNotNewAndIsDirtyWhenVersionIsGreaterThanSavedVersion() {
        var subj = new TestAggregate(
                new TestAggregateId(1),
                new AggregateVersion<>(2, List.of(), 1));
        assertThat(subj.isNew()).isFalse();
        assertThat(subj.isDirty()).isTrue();
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
