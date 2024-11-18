package net.kremianskii.raiz;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public abstract class AggregateId<T> {
    public final T value;

    protected AggregateId(T value) {
        this.value = requireNonNull(value, "value must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AggregateId<?> that = (AggregateId<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
