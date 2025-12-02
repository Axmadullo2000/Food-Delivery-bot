package uz.pdp.restaurantproject.repository;

import uz.pdp.restaurantproject.criteria.BaseCriteria;
import uz.pdp.restaurantproject.model.dto.DataDto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface CrudRepository<M, I> {

    /**
     * @param id this entity id
     * @return entity optional
     */
    Optional<M> findById(I id);

    M save(M m);

    void delete(M m);

    DataDto<List<M>> findAll(BaseCriteria criteria);

    default List<M> findAll() {
        return Collections.emptyList();
    }
}
