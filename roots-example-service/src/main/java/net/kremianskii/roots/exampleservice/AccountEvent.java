package net.kremianskii.roots.exampleservice;

import net.kremianskii.roots.AggregateEvent;

public sealed class AccountEvent extends AggregateEvent<AccountId>
        permits AccountEvent.AccountCreated, AccountEvent.BalanceChanged {
    protected AccountEvent(AccountId aggregateId) {
        super(aggregateId);
    }

    public static final class AccountCreated extends AccountEvent {
        public final int balance;

        public AccountCreated(AccountId aggregateId, int balance) {
            super(aggregateId);
            this.balance = balance;
        }
    }

    public static final class BalanceChanged extends AccountEvent {
        public final int balance;
        public final int prevBalance;

        public BalanceChanged(AccountId aggregateId, int balance, int prevBalance) {
            super(aggregateId);
            this.balance = balance;
            this.prevBalance = prevBalance;
        }
    }
}
