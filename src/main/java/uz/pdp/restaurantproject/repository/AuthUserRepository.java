package uz.pdp.restaurantproject.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import uz.pdp.restaurantproject.config.JPAConfig;
import uz.pdp.restaurantproject.criteria.BaseCriteria;
import uz.pdp.restaurantproject.model.AuthUser;
import uz.pdp.restaurantproject.model.dto.DataDto;

import java.util.List;
import java.util.Optional;

public class AuthUserRepository implements CrudRepository<AuthUser, String> {
    @Override
    public Optional<AuthUser> findById(String id) {
        return Optional.empty();
    }

    @Override
    public AuthUser save(AuthUser authUser) {
        return null;
    }

    @Override
    public void delete(AuthUser authUser) {

    }

    @Override
    public DataDto<List<AuthUser>> findAll(BaseCriteria criteria) {
        EntityManager em = JPAConfig.getEntityManager();
        TypedQuery<AuthUser> query = em.createQuery("from AuthUser", AuthUser.class);
        List<AuthUser> resultList = query.getResultList();
        em.close();
        return new DataDto<>(resultList, 0);
    }
}
