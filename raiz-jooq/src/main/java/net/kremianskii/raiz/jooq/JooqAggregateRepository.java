package net.kremianskii.raiz.jooq;

import net.kremianskii.raiz.Aggregate;
import net.kremianskii.raiz.AggregateEvent;
import net.kremianskii.raiz.AggregateId;
import net.kremianskii.raiz.AggregateRepository;
import net.kremianskii.raiz.OptimisticLockingException;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

import java.sql.Timestamp;
import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static net.kremianskii.raiz.jooq.EventsDatabaseSchema.EVENT;
import static net.kremianskii.raiz.jooq.EventsDatabaseSchema.EVENT_AGGREGATE_ID;
import static net.kremianskii.raiz.jooq.EventsDatabaseSchema.EVENT_DATA;
import static net.kremianskii.raiz.jooq.EventsDatabaseSchema.EVENT_TIMESTAMP;
import static net.kremianskii.raiz.jooq.EventsDatabaseSchema.EVENT_TYPE;

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
                        null,
                        Timestamp.from(clock.instant()))
                .execute();
    }

    protected abstract void insert(DSLContext dsl, AG aggregate);

    protected abstract int update(DSLContext dsl, AG aggregate);
}
