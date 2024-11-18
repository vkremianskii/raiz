package net.kremianskii.raiz.exampleservice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountTests {
    @Test
    void depositsMoney() {
        // given
        var subj = new Account(AccountId.random(), 0);

        // when
        var newSubj = subj.deposit(100);

        // then
        assertEquals(100, newSubj.balance);
    }

    @Test
    void withdrawsMoney() {
        // given
        var subj = new Account(AccountId.random(), 100);

        // when
        var newSubj = subj.withdraw(100);

        // then
        assertEquals(0, newSubj.balance);
    }

    @Test
    void withdrawThrowsWhenAmountExceedsBalance() {
        var subj = new Account(AccountId.random(), 0);
        assertThrows(IllegalStateException.class, () -> subj.withdraw(1));
    }
}
