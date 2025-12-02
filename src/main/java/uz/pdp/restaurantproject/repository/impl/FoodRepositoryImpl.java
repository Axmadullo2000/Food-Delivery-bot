package uz.pdp.restaurantproject.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import uz.pdp.restaurantproject.config.JPAConfig;
import uz.pdp.restaurantproject.criteria.BaseCriteria;
import uz.pdp.restaurantproject.model.Food;
import uz.pdp.restaurantproject.model.dto.DataDto;
import uz.pdp.restaurantproject.repository.FoodRepository;

import java.util.List;
import java.util.Optional;

public class FoodRepositoryImpl implements FoodRepository {
    private static FoodRepositoryImpl instance;

    public static FoodRepository getInstance() {
        if (instance == null) {
            instance = new FoodRepositoryImpl();
        }
        return instance;
    }

    @Override
    public Optional<Food> findById(String id) {
        EntityManager entityManager = JPAConfig.getEntityManager();
        Food food = entityManager.find(Food.class, id);
        entityManager.close();
        return Optional.ofNullable(food);
    }

    @Override
    public Food save(Food food) {
        EntityManager entityManager = JPAConfig.getEntityManager();
        entityManager.getTransaction().begin();
        if (findById(food.getId()).isPresent()) {
            entityManager.merge(food);
        } else {
            entityManager.persist(food);
        }
        entityManager.getTransaction().commit();
        entityManager.close();
        return food;
    }

    @Override
    public void delete(Food food) {
        EntityManager entityManager = JPAConfig.getEntityManager();
        entityManager.getTransaction().begin();
        food.setDeleted(true);
        entityManager.merge(food);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Override
    public DataDto<List<Food>> findAll(BaseCriteria criteria) {
        EntityManager em = JPAConfig.getEntityManager();

        // 1. Qidiruv so'zi bo'yicha filtr
        String search = criteria.getSearch() == null ? "" : criteria.getSearch().toLowerCase().trim();
        boolean hasSearch = !search.isEmpty();

        // JPQL asosiy query
        StringBuilder jpql = new StringBuilder("SELECT f FROM Food f WHERE f.deleted = false");
        if (hasSearch) {
            jpql.append(" AND LOWER(f.name) LIKE LOWER(:search)");
        }
        jpql.append(" ORDER BY f.id DESC"); // yoki boshqa kerakli sort

        TypedQuery<Food> query = em.createQuery(jpql.toString(), Food.class);

        if (hasSearch) {
            query.setParameter("search", "%" + search + "%");
        }

        // Pagination
        int page = criteria.getPage() != null ? criteria.getPage() : 0;
        int size = criteria.getSize() != null && criteria.getSize() > 0 ? criteria.getSize() : 20;

        query.setFirstResult(page * size);
        query.setMaxResults(size);

        // 2. Umumiy soni (totalElements)
        StringBuilder countJpql = new StringBuilder("SELECT COUNT(f) FROM Food f WHERE f.deleted = false");
        if (hasSearch) {
            countJpql.append(" AND LOWER(f.name) LIKE LOWER(:search)");
        }

        TypedQuery<Long> countQuery = em.createQuery(countJpql.toString(), Long.class);
        if (hasSearch) {
            countQuery.setParameter("search", "%" + search + "%");
        }

        Long totalElements = countQuery.getSingleResult();
        int totalPages = (int) ((totalElements + size - 1) / size); // to'g'ri hisoblash

        return new DataDto<>(
                query.getResultList(),
                totalPages
        );
    }

    @Override
    public List<Food> findAll() {
        EntityManager em = JPAConfig.getEntityManager();
        TypedQuery<Food> query = em.createQuery("FROM Food WHERE NOT deleted", Food.class);
        return query.getResultList();

    }
}
