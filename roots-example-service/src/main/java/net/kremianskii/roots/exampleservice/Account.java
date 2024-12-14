package net.kremianskii.roots.exampleservice;

import net.kremianskii.roots.Aggregate;
import net.kremianskii.roots.AggregateVersion;
import net.kremianskii.roots.exampleservice.AccountEvent.AccountCreated;
import net.kremianskii.roots.exampleservice.AccountEvent.BalanceChanged;

import java.util.List;

import static net.kremianskii.roots.AggregateVersion.newVersion;
import static net.kremianskii.roots.util.Checks.check;
import static net.kremianskii.roots.util.Checks.requireNonNegative;
import static net.kremianskii.roots.util.Checks.requirePositive;

public final class Account extends Aggregate<AccountId, AccountEvent> {
    public final int balance;

    public Account(AccountId id, int balance) {
        this(id, balance, newVersion(new AccountCreated(id, balance)));
    }

    public Account(AccountId id, int balance, AggregateVersion<AccountId, AccountEvent> version) {
        super(id, version);
        this.balance = requireNonNegative("balance", balance);
    }

    public Account deposit(int amount) {
        requirePositive("amount", amount);
        return update(
                balance + amount,
                new BalanceChanged(id, balance + amount, balance));
    }

    public Account withdraw(int amount) {
        requirePositive("amount", amount);
        check(amount <= balance, () -> "amount [%d] must not exceed balance [%d]".formatted(amount, balance));
        return update(
                balance - amount,
                new BalanceChanged(id, balance - amount, balance));
    }

    public Account update(int balance, AccountEvent event) {
        return new Account(id, balance, version.advance(List.of(event)));
    }
}
