package uz.pdp.restaurantproject.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import uz.pdp.restaurantproject.config.JPAConfig;
import uz.pdp.restaurantproject.criteria.BaseCriteria;
import uz.pdp.restaurantproject.model.AuthUser;
import uz.pdp.restaurantproject.model.dto.DataDto;
import uz.pdp.restaurantproject.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public final class AuthUserRepository implements CrudRepository<AuthUser, String> {

    private static final AuthUserRepository INSTANCE = new AuthUserRepository();

    private AuthUserRepository() {}

    public static AuthUserRepository getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<AuthUser> findById(String id) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            return Optional.ofNullable(em.find(AuthUser.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public AuthUser save(AuthUser authUser) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (authUser.getId() != null && em.find(AuthUser.class, authUser.getId()) != null) {
                authUser = em.merge(authUser);
            } else {
                em.persist(authUser);
            }
            tx.commit();
            return authUser;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(AuthUser authUser) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            AuthUser managedUser = em.contains(authUser) ? authUser : em.merge(authUser);
            em.remove(managedUser);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public DataDto<List<AuthUser>> findAll(BaseCriteria criteria) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            boolean hasSearch = criteria != null
                    && criteria.getSearch() != null
                    && !criteria.getSearch().isBlank();

            String where = hasSearch
                    ? " WHERE LOWER(a.name) LIKE :search OR LOWER(a.username) LIKE :search"
                    : "";

            TypedQuery<AuthUser> query = em.createQuery("SELECT a FROM AuthUser a" + where, AuthUser.class);
            TypedQuery<Long> countQuery = em.createQuery("SELECT COUNT(a) FROM AuthUser a" + where, Long.class);

            if (hasSearch) {
                String searchPattern = "%" + criteria.getSearch().toLowerCase() + "%";
                query.setParameter("search", searchPattern);
                countQuery.setParameter("search", searchPattern);
            }

            int page = criteria != null && criteria.getPage() != null ? criteria.getPage() : 0;
            int size = criteria != null && criteria.getSize() != null && criteria.getSize() > 0
                    ? criteria.getSize() : 20;
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            List<AuthUser> users = query.getResultList();
            long total = countQuery.getSingleResult();
            int totalPages = (int) ((total + size - 1) / size);

            return new DataDto<>(users, totalPages);
        } finally {
            em.close();
        }
    }

    @Override
    public List<AuthUser> findAll() {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            return em.createQuery("SELECT a FROM AuthUser a", AuthUser.class).getResultList();
        } finally {
            em.close();
        }
    }
}
