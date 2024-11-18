package net.kremianskii.raiz.exampleservice;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.SQLDataType;

import java.util.UUID;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;
import static org.jooq.impl.SQLDataType.INTEGER;

public final class DatabaseSchema {
    public static final Table<?> ACCOUNT = table("ACCOUNT");
    public static final Field<UUID> ACCOUNT_ID = field("ID", SQLDataType.UUID.notNull());
    public static final Field<Integer> ACCOUNT_BALANCE = field("BALANCE", INTEGER.notNull());
    public static final Field<Integer> ACCOUNT_VERSION = field("VERSION", INTEGER.notNull());

    private DatabaseSchema() {
    }

    public static void migrate(DSLContext dsl) {
        dsl.createTableIfNotExists(ACCOUNT)
                .columns(ACCOUNT_ID, ACCOUNT_BALANCE, ACCOUNT_VERSION)
                .primaryKey(ACCOUNT_ID)
                .execute();
    }
}
