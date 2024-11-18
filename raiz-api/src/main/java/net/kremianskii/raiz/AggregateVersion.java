package net.kremianskii.raiz;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static net.kremianskii.raiz.util.Checks.check;
import static net.kremianskii.raiz.util.Checks.requirePositive;

public record AggregateVersion<ID extends AggregateId<?>, EV extends AggregateEvent<ID>>(
        int version,
        List<EV> events,
        @Nullable Integer savedVersion) {
    public AggregateVersion {
        requirePositive("version", version);
        requireNonNull(events, "events must not be null");
        if (savedVersion != null) {
            check(savedVersion <= version,
                    () -> "savedVersion [%d] must be less than or equal to version [%d]".formatted(
                            savedVersion, version));
        }
    }

    public AggregateVersion<ID, EV> advance(List<EV> newEvents) {
        requireNonNull(newEvents, "newEvents must not be null");
        var allEvents = new ArrayList<>(this.events);
        allEvents.addAll(newEvents);
        return new AggregateVersion<>(version + 1, allEvents, savedVersion);
    }

    public boolean isNew() {
        return savedVersion == null;
    }

    public boolean isDirty() {
        return savedVersion == null || version != savedVersion;
    }

    public static <ID extends AggregateId<?>, EV extends AggregateEvent<ID>> AggregateVersion<ID, EV> newVersion() {
        return new AggregateVersion<>(1, List.of(), null);
    }

    public static <ID extends AggregateId<?>, EV extends AggregateEvent<ID>>
    AggregateVersion<ID, EV> newVersion(EV createdEvent) {
        return new AggregateVersion<>(1, List.of(createdEvent), null);
    }

    public static <ID extends AggregateId<?>, EV extends AggregateEvent<ID>>
    AggregateVersion<ID, EV> savedVersion(int version) {
        return new AggregateVersion<>(version, List.of(), version);
    }
}
