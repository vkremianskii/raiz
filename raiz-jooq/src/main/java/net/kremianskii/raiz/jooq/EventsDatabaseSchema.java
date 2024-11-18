package net.kremianskii.raiz.jooq;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;

import java.sql.Timestamp;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;
import static org.jooq.impl.SQLDataType.BIGINT;
import static org.jooq.impl.SQLDataType.TIMESTAMP;
import static org.jooq.impl.SQLDataType.VARCHAR;

public final class EventsDatabaseSchema {
    public static final Table<?> EVENT = table("EVENT");
    public static final Field<?> EVENT_ID = field("ID", BIGINT.notNull().identity(true));
    public static final Field<String> EVENT_AGGREGATE_ID = field("AGGREGATE_ID", VARCHAR.notNull());
    public static final Field<String> EVENT_TYPE = field("TYPE", VARCHAR.notNull());
    public static final Field<String> EVENT_DATA = field("DATA", VARCHAR);
    public static final Field<Timestamp> EVENT_TIMESTAMP = field("TIMESTAMP", TIMESTAMP.notNull());

    private EventsDatabaseSchema() {
    }

    public static void migrate(DSLContext dsl) {
        dsl.createTableIfNotExists(EVENT)
                .columns(EVENT_ID, EVENT_AGGREGATE_ID, EVENT_TYPE, EVENT_DATA, EVENT_TIMESTAMP)
                .primaryKey(EVENT_ID)
                .execute();
        dsl.createIndexIfNotExists("event_aggregate_id")
                .on(EVENT, EVENT_AGGREGATE_ID)
                .execute();
    }
}
