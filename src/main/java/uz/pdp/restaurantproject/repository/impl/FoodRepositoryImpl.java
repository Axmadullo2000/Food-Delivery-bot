package uz.pdp.restaurantproject.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import uz.pdp.restaurantproject.config.JPAConfig;
import uz.pdp.restaurantproject.criteria.BaseCriteria;
import uz.pdp.restaurantproject.model.Food;
import uz.pdp.restaurantproject.model.dto.DataDto;
import uz.pdp.restaurantproject.repository.FoodRepository;

import java.util.List;
import java.util.Optional;

public final class FoodRepositoryImpl implements FoodRepository {
    private static final int DEFAULT_PAGE_SIZE = 20;

    private static final FoodRepositoryImpl INSTANCE = new FoodRepositoryImpl();

    private FoodRepositoryImpl() {}

    public static FoodRepository getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<Food> findById(String id) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Food.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public Food save(Food food) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Food managed;
            if (food.getId() != null && em.find(Food.class, food.getId()) != null) {
                managed = em.merge(food);
            } else {
                em.persist(food);
                managed = food;
            }
            tx.commit();
            return managed;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Food food) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            food.setDeleted(true);
            em.merge(food);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public DataDto<List<Food>> findAll(BaseCriteria criteria) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            String search = criteria.getSearch() == null ? "" : criteria.getSearch().toLowerCase().trim();
            boolean hasSearch = !search.isEmpty();

            String whereClause = "WHERE f.deleted = false"
                    + (hasSearch ? " AND LOWER(f.name) LIKE :search" : "");

            TypedQuery<Food> query = em.createQuery(
                    "SELECT f FROM Food f " + whereClause + " ORDER BY f.id DESC",
                    Food.class);
            TypedQuery<Long> countQuery = em.createQuery(
                    "SELECT COUNT(f) FROM Food f " + whereClause,
                    Long.class);

            if (hasSearch) {
                String pattern = "%" + search + "%";
                query.setParameter("search", pattern);
                countQuery.setParameter("search", pattern);
            }

            int page = criteria.getPage() != null ? criteria.getPage() : 0;
            int size = criteria.getSize() != null && criteria.getSize() > 0 ? criteria.getSize() : DEFAULT_PAGE_SIZE;
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            long totalElements = countQuery.getSingleResult();
            int totalPages = (int) ((totalElements + size - 1) / size);

            return new DataDto<>(query.getResultList(), totalPages);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Food> findAll() {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            return em.createQuery("FROM Food WHERE NOT deleted", Food.class).getResultList();
        } finally {
            em.close();
        }
    }
}
