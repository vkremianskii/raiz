package net.kremianskii.roots;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface AggregateRepository<
        ID extends AggregateId<?>,
        EV extends AggregateEvent<ID>,
        AG extends Aggregate<ID, EV>> {
    Optional<AG> find(ID id);

    default AG get(ID id) {
        return find(id).orElseThrow(NoSuchElementException::new);
    }

    List<AG> findAll(Collection<ID> ids);

    void save(AG aggregate);

    void saveAll(Collection<AG> aggregates);
}
