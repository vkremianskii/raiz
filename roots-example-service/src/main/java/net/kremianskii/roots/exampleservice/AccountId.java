package net.kremianskii.roots.exampleservice;

import net.kremianskii.roots.AggregateId;

import java.util.UUID;

import static java.util.UUID.randomUUID;

public final class AccountId extends AggregateId<UUID> {
    private AccountId(UUID value) {
        super(value);
    }

    public static AccountId accountId(UUID value) {
        return new AccountId(value);
    }

    public static AccountId random() {
        return new AccountId(randomUUID());
    }
}
