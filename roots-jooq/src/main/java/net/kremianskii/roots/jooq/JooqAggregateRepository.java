package net.kremianskii.roots.jooq;

import com.fasterxml.jackson.databind.node.ObjectNode;
import net.kremianskii.roots.Aggregate;
import net.kremianskii.roots.AggregateEvent;
import net.kremianskii.roots.AggregateId;
import net.kremianskii.roots.AggregateRepository;
import net.kremianskii.roots.OptimisticLockingException;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static net.kremianskii.roots.jooq.EventsDatabaseSchema.EVENT;
import static net.kremianskii.roots.jooq.EventsDatabaseSchema.EVENT_AGGREGATE_ID;
import static net.kremianskii.roots.jooq.EventsDatabaseSchema.EVENT_DATA;
import static net.kremianskii.roots.jooq.EventsDatabaseSchema.EVENT_TIMESTAMP;
import static net.kremianskii.roots.jooq.EventsDatabaseSchema.EVENT_TYPE;
import static net.kremianskii.roots.jooq.Json.jsonStringFromValue;

public abstract class JooqAggregateRepository<
        IDV,
        ID extends AggregateId<IDV>,
        EV extends AggregateEvent<ID>,
        AG extends Aggregate<ID, EV>> implements AggregateRepository<ID, EV, AG> {
    private final Clock clock;
    private final DSLContext dsl;
    private final Table<?> table;
    private final Field<IDV> idField;

    protected JooqAggregateRepository(Clock clock, DSLContext dsl, Table<?> table, Field<IDV> idField) {
        this.clock = requireNonNull(clock, "clock must not be null");
        this.dsl = requireNonNull(dsl, "dsl must not be null");
        this.table = requireNonNull(table, "table must not be null");
        this.idField = requireNonNull(idField, "idField must not be null");
    }

    @Override
    public Optional<AG> find(ID id) {
        return dsl.select(table.asterisk())
                .from(table)
                .where(idField.eq(id.value))
                .fetchOptional(this::aggregateFromRecord);
    }

    @Override
    public List<AG> findAll(Collection<ID> ids) {
        return dsl.select(table.asterisk())
                .from(table)
                .where(idField.in(ids.stream().map(id -> id.value).toList()))
                .fetch(this::aggregateFromRecord);
    }

    protected abstract AG aggregateFromRecord(Record record);

    @Override
    public void save(AG aggregate) {
        save(dsl, aggregate);
    }

    @Override
    public void saveAll(Collection<AG> aggregates) {
        dsl.transaction(tx -> aggregates.forEach(e -> save(tx.dsl(), e)));
    }

    private void save(DSLContext dsl, AG aggregate) {
        if (!aggregate.isDirty()) return;
        dsl.transaction(tx -> {
            if (aggregate.isNew()) {
                insert(dsl, aggregate);
            } else {
                int count = update(dsl, aggregate);
                if (count == 0) {
                    throw new OptimisticLockingException();
                }
            }
            aggregate.version.events().forEach(this::insertEvent);
        });
    }

    private void insertEvent(EV event) {
        dsl.insertInto(EVENT)
                .columns(EVENT_AGGREGATE_ID, EVENT_TYPE, EVENT_DATA, EVENT_TIMESTAMP)
                .values(event.aggregateId.toString(),
                        event.getClass().getSimpleName(),
                        jsonStringFromValue(event),
                        Timestamp.from(clock.instant()))
                .execute();
    }

    protected abstract void insert(DSLContext dsl, AG aggregate);

    protected abstract int update(DSLContext dsl, AG aggregate);

    public List<PersistedAggregateEvent> findAllEvents(ID aggregateId) {
        return dsl.select(EVENT.asterisk())
                .from(EVENT)
                .where(EVENT_AGGREGATE_ID.eq(aggregateId.toString()))
                .fetch(this::eventFromRecord);
    }

    private PersistedAggregateEvent eventFromRecord(Record record) {
        return new PersistedAggregateEvent(
                record.get(EVENT_AGGREGATE_ID),
                record.get(EVENT_TYPE),
                (ObjectNode) Json.treeFromJsonString(record.get(EVENT_DATA)),
                record.get(EVENT_TIMESTAMP).toInstant()
        );
    }

    public record PersistedAggregateEvent(
            String aggregateId,
            String type,
            ObjectNode data,
            Instant timestamp
    ) {
    }
}
