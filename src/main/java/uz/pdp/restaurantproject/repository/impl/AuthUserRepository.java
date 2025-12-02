package uz.pdp.restaurantproject.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import uz.pdp.restaurantproject.config.JPAConfig;
import uz.pdp.restaurantproject.criteria.BaseCriteria;
import uz.pdp.restaurantproject.model.AuthUser;
import uz.pdp.restaurantproject.model.dto.DataDto;
import uz.pdp.restaurantproject.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public class AuthUserRepository implements CrudRepository<AuthUser, String> {

    private static AuthUserRepository instance;

    private AuthUserRepository() {}

    public static AuthUserRepository getInstance() {
        if (instance == null) {
            instance = new AuthUserRepository();
        }
        return instance;
    }

    @Override
    public Optional<AuthUser> findById(String id) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            AuthUser user = em.find(AuthUser.class, id);
            return Optional.ofNullable(user);
        } finally {
            em.close();
        }
    }

    @Override
    public AuthUser save(AuthUser authUser) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            if (authUser.getId() == null) {
                em.persist(authUser);
            } else {
                authUser = em.merge(authUser);
            }
            em.getTransaction().commit();
            return authUser;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(AuthUser authUser) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            AuthUser managedUser = em.contains(authUser) ? authUser : em.merge(authUser);
            em.remove(managedUser);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public DataDto<List<AuthUser>> findAll(BaseCriteria criteria) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            String baseQuery = "SELECT a FROM AuthUser a";
            String countQueryStr = "SELECT COUNT(a) FROM AuthUser a";

            if (criteria.getSearch() != null && !criteria.getSearch().isBlank()) {
                baseQuery += " WHERE LOWER(a.name) LIKE :search OR LOWER(a.username) LIKE :search";
                countQueryStr += " WHERE LOWER(a.name) LIKE :search OR LOWER(a.username) LIKE :search";
            }

            TypedQuery<AuthUser> query = em.createQuery(baseQuery, AuthUser.class);
            TypedQuery<Long> countQuery = em.createQuery(countQueryStr, Long.class);

            if (criteria.getSearch() != null && !criteria.getSearch().isBlank()) {
                String searchPattern = "%" + criteria.getSearch().toLowerCase() + "%";
                query.setParameter("search", searchPattern);
                countQuery.setParameter("search", searchPattern);
            }

            // Пагинация
            query.setFirstResult(criteria.getPage() * criteria.getSize());
            query.setMaxResults(criteria.getSize());

            List<AuthUser> users = query.getResultList();
            Long total = countQuery.getSingleResult();

            return new DataDto<>(users, total.intValue());
        } finally {
            em.close();
        }
    }

    @Override
    public List<AuthUser> findAll() {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            TypedQuery<AuthUser> query = em.createQuery("SELECT a FROM AuthUser a", AuthUser.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
