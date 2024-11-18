package net.kremianskii.raiz;

import static java.util.Objects.requireNonNull;

public class AggregateEvent<ID extends AggregateId<?>> {
    public final ID aggregateId;

    protected AggregateEvent(ID aggregateId) {
        this.aggregateId = requireNonNull(aggregateId, "aggregateId must not be null");
    }
}
