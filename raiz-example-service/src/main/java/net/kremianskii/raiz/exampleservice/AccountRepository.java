package net.kremianskii.raiz.exampleservice;

import net.kremianskii.raiz.jooq.JooqAggregateRepository;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.time.Clock;
import java.util.UUID;

import static net.kremianskii.raiz.AggregateVersion.savedVersion;
import static net.kremianskii.raiz.exampleservice.AccountId.accountId;
import static net.kremianskii.raiz.exampleservice.DatabaseSchema.ACCOUNT;
import static net.kremianskii.raiz.exampleservice.DatabaseSchema.ACCOUNT_BALANCE;
import static net.kremianskii.raiz.exampleservice.DatabaseSchema.ACCOUNT_ID;
import static net.kremianskii.raiz.exampleservice.DatabaseSchema.ACCOUNT_VERSION;

public final class AccountRepository extends JooqAggregateRepository<UUID, AccountId, AccountEvent, Account> {
    public AccountRepository(Clock clock, DSLContext dsl) {
        super(clock, dsl, ACCOUNT, ACCOUNT_ID);
    }

    @Override
    protected Account aggregateFromRecord(Record record) {
        return new Account(
                accountId(record.get(ACCOUNT_ID)),
                record.get(ACCOUNT_BALANCE),
                savedVersion(record.get(ACCOUNT_VERSION)));
    }

    @Override
    protected void insert(DSLContext dsl, Account aggregate) {
        dsl.insertInto(ACCOUNT, ACCOUNT_ID, ACCOUNT_BALANCE, ACCOUNT_VERSION)
                .values(aggregate.id.value, aggregate.balance, aggregate.version.version())
                .execute();
    }

    @Override
    protected int update(DSLContext dsl, Account aggregate) {
        return dsl.update(ACCOUNT)
                .set(ACCOUNT_BALANCE, aggregate.balance)
                .set(ACCOUNT_VERSION, aggregate.version.version())
                .where(ACCOUNT_ID.eq(aggregate.id.value)
                        .and(ACCOUNT_VERSION.eq(aggregate.version.savedVersion())))
                .execute();
    }
}
