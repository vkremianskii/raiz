package net.kremianskii.roots.exampleservice;

import net.kremianskii.roots.exampleservice.LedgerController.TransferRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
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
            var accountsList = accounts.stream().toList();
            assertThat(accountsList).hasSize(2);
            assertThat(accountsList.get(0)).satisfies(account -> {
                assertThat(account.id).isEqualTo(from.id);
                assertThat(account.balance).isEqualTo(0);
            });
            assertThat(accountsList.get(1)).satisfies(account -> {
                assertThat(account.id).isEqualTo(to.id);
                assertThat(account.balance).isEqualTo(100);
            });
        }));
    }
}
