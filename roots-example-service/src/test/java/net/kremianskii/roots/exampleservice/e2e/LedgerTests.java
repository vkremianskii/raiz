package net.kremianskii.roots.exampleservice.e2e;

import net.kremianskii.roots.exampleservice.Account;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.time.Clock.systemUTC;
import static net.kremianskii.roots.exampleservice.AccountId.accountId;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LedgerTests {
    private final Clock clock = systemUTC();
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
        assertEquals(2, accounts.size());
        assertEquals(from.id, accounts.get(0).id);
        assertEquals(0, accounts.get(0).balance);
        assertEquals(to.id, accounts.get(1).id);
        assertEquals(100, accounts.get(1).balance);
    }
}
