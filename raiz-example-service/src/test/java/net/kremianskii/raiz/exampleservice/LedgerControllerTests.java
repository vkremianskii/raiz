package net.kremianskii.raiz.exampleservice;

import net.kremianskii.raiz.exampleservice.LedgerController.TransferRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LedgerControllerTests {
    private final AccountRepository accountRepo = mock(AccountRepository.class);
    private final LedgerController subject = new LedgerController(accountRepo);

    @Test
    void transfersMoneyBetweenAccounts() {
        // given
        var from = new Account(AccountId.random(), 100);
        var to = new Account(AccountId.random(), 0);
        given(accountRepo.findAll(Set.of(from.id, to.id))).willReturn(List.of(from, to));

        // when
        subject.transfer(new TransferRequest(from.id, to.id, 100));

        // then
        verify(accountRepo).saveAll(assertArg(accounts -> {
            assertEquals(2, accounts.size());
            assertSame(from.id, ((List<Account>) accounts).get(0).id);
            assertEquals(0, ((List<Account>) accounts).get(0).balance);
            assertEquals(to.id, ((List<Account>) accounts).get(1).id);
            assertEquals(100, ((List<Account>) accounts).get(1).balance);
        }));
    }
}
