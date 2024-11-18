package net.kremianskii.raiz.exampleservice;

import net.kremianskii.raiz.OptimisticLockingException;
import net.kremianskii.raiz.jooq.EventsDatabaseSchema;
import org.jooq.DSLContext;
import org.jooq.exception.IntegrityConstraintViolationException;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.time.Instant.EPOCH;
import static java.time.ZoneOffset.UTC;
import static net.kremianskii.raiz.AggregateVersion.savedVersion;
import static net.kremianskii.raiz.exampleservice.AccountId.accountId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountRepositoryTests {
    private final Clock clock = Clock.fixed(EPOCH, UTC);
    private final DSLContext dsl = DSL.using("jdbc:h2:mem:");
    private final AccountRepository subj = new AccountRepository(clock, dsl);

    @BeforeEach
    void setup() {
        EventsDatabaseSchema.migrate(dsl);
        DatabaseSchema.migrate(dsl);
    }

    @Test
    void savesAndFindsAccounts() {
        var account1Id = accountId(new UUID(0, 0));
        var account2Id = accountId(new UUID(0, 1));
        {
            // when
            subj.saveAll(List.of(new Account(account1Id, 0), new Account(account2Id, 100)));
            var found = subj.findAll(Set.of(account1Id, account2Id));

            // then
            assertEquals(2, found.size());
            assertEquals(account1Id, found.get(0).id);
            assertEquals(0, found.get(0).balance);
            assertEquals(savedVersion(1), found.get(0).version);
            assertEquals(account2Id, found.get(1).id);
            assertEquals(100, found.get(1).balance);
            assertEquals(savedVersion(1), found.get(1).version);
        }
        {
            // when
            var account = subj.get(account1Id);
            subj.save(account.deposit(100));
            var found = subj.find(account1Id);

            // then
            assertTrue(found.isPresent());
            assertEquals(account1Id, found.get().id);
            assertEquals(100, found.get().balance);
            assertEquals(savedVersion(2), found.get().version);
        }
    }

    @Test
    void saveAllRollbacksTransactionOnError() {
        // given
        var account1 = new Account(AccountId.random(), 0);
        var account2 = new Account(AccountId.random(), 0);
        var account3 = new Account(account2.id, 0);

        // when
        Executable save = () -> subj.saveAll(List.of(account1, account2, account3));
        var found = subj.findAll(Set.of(account1.id, account2.id));

        // then
        assertThrows(IntegrityConstraintViolationException.class, save);
        assertEquals(0, found.size());
    }

    @Test
    void throwsOnInsertionConflict() {
        var account = new Account(AccountId.random(), 0);
        subj.save(account.deposit(100));
        assertThrows(IntegrityConstraintViolationException.class, () -> subj.save(account.deposit(100)));
    }

    @Test
    void throwsOnUpdateConflict() {
        var accountId = AccountId.random();
        subj.save(new Account(accountId, 0));
        var account = subj.get(accountId);
        subj.save(account.deposit(100));
        assertThrows(OptimisticLockingException.class, () -> subj.save(account.deposit(100)));
    }
}
