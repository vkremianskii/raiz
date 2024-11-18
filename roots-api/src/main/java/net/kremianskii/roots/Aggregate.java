package net.kremianskii.roots;

import static java.util.Objects.requireNonNull;

public abstract class Aggregate<ID extends AggregateId<?>, EV extends AggregateEvent<ID>> {
    public final ID id;
    public final AggregateVersion<ID, EV> version;

    protected Aggregate(ID id, AggregateVersion<ID, EV> version) {
        this.id = requireNonNull(id, "id must not be null");
        this.version = requireNonNull(version, "version must not be null");
    }

    public boolean isNew() {
        return version.isNew();
    }

    public boolean isDirty() {
        return version.isDirty();
    }
}
