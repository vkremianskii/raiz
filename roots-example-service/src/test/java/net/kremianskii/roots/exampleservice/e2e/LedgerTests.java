package net.kremianskii.roots.exampleservice.e2e;

import net.kremianskii.roots.exampleservice.Account;
import net.kremianskii.roots.exampleservice.AccountEvent.AccountCreated;
import net.kremianskii.roots.exampleservice.AccountEvent.BalanceChanged;
import net.kremianskii.roots.exampleservice.AccountRepository;
import net.kremianskii.roots.exampleservice.DatabaseSchema;
import net.kremianskii.roots.exampleservice.LedgerController;
import net.kremianskii.roots.exampleservice.LedgerController.TransferRequest;
import net.kremianskii.roots.jooq.EventsDatabaseSchema;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.MILLIS;
import static net.kremianskii.roots.exampleservice.AccountId.accountId;
import static net.kremianskii.roots.jooq.Json.jsonStringFromValue;
import static org.assertj.core.api.Assertions.assertThat;

class LedgerTests {
    private final Instant instant = Instant.now().truncatedTo(MILLIS);
    private final Clock clock = Clock.fixed(instant, UTC);
    private final DSLContext dsl = DSL.using("jdbc:h2:mem:");
    private final AccountRepository accountRepo = new AccountRepository(clock, dsl);
    private final LedgerController controller = new LedgerController(accountRepo);

    @BeforeEach
    void setup() {
        EventsDatabaseSchema.migrate(dsl);
        DatabaseSchema.migrate(dsl);
    }

    @Test
    void transfersMoneyBetweenAccounts() {
        // given
        var from = new Account(accountId(new UUID(0, 0)), 100);
        var to = new Account(accountId(new UUID(0, 1)), 0);
        accountRepo.saveAll(List.of(from, to));

        // when
        controller.transfer(new TransferRequest(from.id, to.id, 100));

        // then
        var accounts = accountRepo.findAll(Set.of(from.id, to.id));
        assertThat(accounts).hasSize(2);
        assertThat(accounts.get(0)).satisfies(account -> {
            assertThat(account.id).isEqualTo(from.id);
            assertThat(account.balance).isEqualTo(0);
        });
        assertThat(accounts.get(1)).satisfies(account -> {
            assertThat(account.id).isEqualTo(to.id);
            assertThat(account.balance).isEqualTo(100);
        });

        var fromAccountEvents = accountRepo.findAllEvents(from.id);
        assertThat(fromAccountEvents).hasSize(2);
        assertThat(fromAccountEvents.get(0)).satisfies(event -> {
            assertThat(event.aggregateId()).isEqualTo(from.id.toString());
            assertThat(event.type()).isEqualTo(AccountCreated.class.getSimpleName());
            assertThat(event.timestamp()).isEqualTo(instant);
        });
        assertThat(fromAccountEvents.get(1)).satisfies(event -> {
            assertThat(event.aggregateId()).isEqualTo(from.id.toString());
            assertThat(event.type()).isEqualTo(BalanceChanged.class.getSimpleName());
            assertThat(event.data().toString())
                    .isEqualTo(jsonStringFromValue(new BalanceChanged(from.id, 0, 100)));
            assertThat(event.timestamp()).isEqualTo(instant);
        });

        var toAccountEvents = accountRepo.findAllEvents(to.id);
        assertThat(toAccountEvents).hasSize(2);
        assertThat(toAccountEvents.get(0)).satisfies(event -> {
            assertThat(event.aggregateId()).isEqualTo(to.id.toString());
            assertThat(event.type()).isEqualTo(AccountCreated.class.getSimpleName());
            assertThat(event.timestamp()).isEqualTo(instant);
        });
        assertThat(toAccountEvents.get(1)).satisfies(event -> {
            assertThat(event.aggregateId()).isEqualTo(to.id.toString());
            assertThat(event.type()).isEqualTo(BalanceChanged.class.getSimpleName());
            assertThat(event.data().toString())
                    .isEqualTo(jsonStringFromValue(new BalanceChanged(to.id, 100, 0)));
            assertThat(event.timestamp()).isEqualTo(instant);
        });
    }
}
