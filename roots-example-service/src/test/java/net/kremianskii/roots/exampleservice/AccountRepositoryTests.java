package net.kremianskii.roots.exampleservice;

import net.kremianskii.roots.OptimisticLockingException;
import net.kremianskii.roots.jooq.EventsDatabaseSchema;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.jooq.DSLContext;
import org.jooq.exception.IntegrityConstraintViolationException;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.time.Instant.EPOCH;
import static java.time.ZoneOffset.UTC;
import static net.kremianskii.roots.AggregateVersion.savedVersion;
import static net.kremianskii.roots.exampleservice.AccountId.accountId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
            assertThat(found).hasSize(2);
            assertThat(found.get(0)).satisfies(account -> {
                assertThat(account.id).isEqualTo(account1Id);
                assertThat(account.balance).isEqualTo(0);
                assertThat(account.version).isEqualTo(savedVersion(1));
            });
            assertThat(found.get(1)).satisfies(account -> {
                assertThat(account.id).isEqualTo(account2Id);
                assertThat(account.balance).isEqualTo(100);
                assertThat(account.version).isEqualTo(savedVersion(1));
            });
        }
        {
            // when
            var account1 = subj.get(account1Id);
            subj.save(account1.deposit(100));
            var found = subj.find(account1Id);

            // then
            assertThat(found).isPresent();
            assertThat(found.get()).satisfies(account -> {
                assertThat(account.id).isEqualTo(account1Id);
                assertThat(account.balance).isEqualTo(100);
                assertThat(account.version).isEqualTo(savedVersion(2));
            });
        }
    }

    @Test
    void saveAllRollbacksTransactionOnError() {
        // given
        var account1 = new Account(AccountId.random(), 0);
        var account2 = new Account(AccountId.random(), 0);
        var account3 = new Account(account2.id, 0);

        // when
        ThrowingCallable save = () -> subj.saveAll(List.of(account1, account2, account3));
        var found = subj.findAll(Set.of(account1.id, account2.id));

        // then
        assertThatThrownBy(save).isInstanceOf(IntegrityConstraintViolationException.class);
        assertThat(found).isEmpty();
    }

    @Test
    void throwsOnInsertionConflict() {
        var account = new Account(AccountId.random(), 0);
        subj.save(account.deposit(100));
        assertThatThrownBy(() -> subj.save(account.deposit(100)))
                .isInstanceOf(IntegrityConstraintViolationException.class);
    }

    @Test
    void throwsOnUpdateConflict() {
        var accountId = AccountId.random();
        subj.save(new Account(accountId, 0));
        var account = subj.get(accountId);
        subj.save(account.deposit(100));
        assertThatThrownBy(() -> subj.save(account.deposit(100)))
                .isInstanceOf(OptimisticLockingException.class);
    }
}
