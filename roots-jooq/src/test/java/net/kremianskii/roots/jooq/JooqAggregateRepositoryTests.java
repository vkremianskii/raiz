package net.kremianskii.roots.jooq;

import net.kremianskii.roots.Aggregate;
import net.kremianskii.roots.AggregateEvent;
import net.kremianskii.roots.AggregateId;
import net.kremianskii.roots.AggregateVersion;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

import static java.time.Instant.EPOCH;
import static java.time.ZoneOffset.UTC;
import static java.util.UUID.randomUUID;
import static net.kremianskii.roots.AggregateVersion.newVersion;
import static net.kremianskii.roots.AggregateVersion.savedVersion;
import static net.kremianskii.roots.jooq.EventsDatabaseSchema.EVENT;
import static net.kremianskii.roots.jooq.EventsDatabaseSchema.EVENT_AGGREGATE_ID;
import static net.kremianskii.roots.jooq.EventsDatabaseSchema.EVENT_DATA;
import static net.kremianskii.roots.jooq.EventsDatabaseSchema.EVENT_TIMESTAMP;
import static net.kremianskii.roots.jooq.EventsDatabaseSchema.EVENT_TYPE;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;
import static org.jooq.impl.SQLDataType.INTEGER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JooqAggregateRepositoryTests {
    private static final Table<?> TEST_AGGREGATE = table("TEST_AGGREGATE");
    private static final Field<UUID> TEST_AGGREGATE_ID = field("ID", SQLDataType.UUID.notNull());
    private static final Field<Integer> TEST_AGGREGATE_VERSION = field("VERSION", INTEGER.notNull());

    private final Clock clock = Clock.fixed(EPOCH, UTC);
    private final DSLContext dsl = DSL.using("jdbc:h2:mem:");
    private final TestAggregateRepository subj = new TestAggregateRepository(
            clock, dsl, TEST_AGGREGATE, TEST_AGGREGATE_ID);

    @BeforeEach
    void setup() {
        EventsDatabaseSchema.migrate(dsl);
        dsl.createTableIfNotExists(TEST_AGGREGATE)
                .columns(TEST_AGGREGATE_ID, TEST_AGGREGATE_VERSION)
                .primaryKey(TEST_AGGREGATE_ID)
                .execute();
    }

    @Test
    void insertsEventsOnSave() {
        // given
        var aggregateId = new TestAggregateId(randomUUID());
        var aggregate = new TestAggregate(aggregateId, newVersion(new TestAggregateEvent(aggregateId)));

        // when
        subj.save(aggregate);

        // then
        var events = dsl.select(EVENT_TYPE, EVENT_DATA, EVENT_TIMESTAMP)
                .from(EVENT)
                .where(EVENT_AGGREGATE_ID.eq(aggregate.id.toString()))
                .fetch();
        assertEquals(1, events.size());
        assertEquals(TestAggregateEvent.class.getSimpleName(), events.get(0).get(EVENT_TYPE));
        assertNull(events.get(0).get(EVENT_DATA));
        assertEquals(Timestamp.from(EPOCH), events.get(0).get(EVENT_TIMESTAMP));
    }

    private static final class TestAggregateRepository
            extends JooqAggregateRepository<UUID, TestAggregateId, TestAggregateEvent, TestAggregate> {
        TestAggregateRepository(Clock clock, DSLContext dsl, Table<?> table, Field<UUID> idField) {
            super(clock, dsl, table, idField);
        }

        @Override
        protected TestAggregate aggregateFromRecord(Record record) {
            return new TestAggregate(
                    new TestAggregateId(record.get(TEST_AGGREGATE_ID)),
                    savedVersion(record.get(TEST_AGGREGATE_VERSION)));
        }

        @Override
        protected void insert(DSLContext dsl, TestAggregate aggregate) {

        }

        @Override
        protected int update(DSLContext dsl, TestAggregate aggregate) {
            return 0;
        }
    }

    private static final class TestAggregate extends Aggregate<TestAggregateId, TestAggregateEvent> {
        TestAggregate(TestAggregateId id, AggregateVersion<TestAggregateId, TestAggregateEvent> version) {
            super(id, version);
        }

        TestAggregate update(List<TestAggregateEvent> events) {
            return new TestAggregate(id, version.advance(events));
        }
    }

    private static final class TestAggregateId extends AggregateId<UUID> {
        TestAggregateId(UUID value) {
            super(value);
        }
    }

    private static final class TestAggregateEvent extends AggregateEvent<TestAggregateId> {
        TestAggregateEvent(TestAggregateId aggregateId) {
            super(aggregateId);
        }
    }
}