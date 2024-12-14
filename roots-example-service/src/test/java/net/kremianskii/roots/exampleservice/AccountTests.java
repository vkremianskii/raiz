package net.kremianskii.roots.exampleservice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTests {
    @Test
    void depositsMoney() {
        // given
        var subj = new Account(AccountId.random(), 0);

        // when
        var newSubj = subj.deposit(100);

        // then
        assertThat(newSubj.balance).isEqualTo(100);
    }

    @Test
    void withdrawsMoney() {
        // given
        var subj = new Account(AccountId.random(), 100);

        // when
        var newSubj = subj.withdraw(100);

        // then
        assertThat(newSubj.balance).isEqualTo(0);
    }

    @Test
    void withdrawThrowsWhenAmountExceedsBalance() {
        var subj = new Account(AccountId.random(), 0);
        assertThatThrownBy(() -> subj.withdraw(1)).isInstanceOf(IllegalStateException.class);
    }
}
