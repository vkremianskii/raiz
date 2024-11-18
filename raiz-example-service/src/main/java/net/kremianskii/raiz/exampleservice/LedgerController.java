package net.kremianskii.raiz.exampleservice;

import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static net.kremianskii.raiz.util.Collections.firstOrThrow;

public final class LedgerController {
    private final AccountRepository accountRepo;

    public LedgerController(AccountRepository accountRepo) {
        this.accountRepo = requireNonNull(accountRepo, "accountRepo must not be null");
    }

    public void transfer(TransferRequest request) {
        var accounts = accountRepo.findAll(Set.of(request.from, request.to));
        var fromAccount = firstOrThrow(accounts, a -> a.id.equals(request.from));
        var toAccount = firstOrThrow(accounts, a -> a.id.equals(request.to));
        accountRepo.saveAll(List.of(
                fromAccount.withdraw(request.amount),
                toAccount.deposit(request.amount)));
    }

    public record TransferRequest(AccountId from, AccountId to, int amount) {
    }
}
