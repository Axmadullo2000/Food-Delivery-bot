package uz.pdp.restaurantproject.repository;

import jakarta.persistence.EntityManager;
import uz.pdp.restaurantproject.config.JPAConfig;
import uz.pdp.restaurantproject.criteria.BaseCriteria;
import uz.pdp.restaurantproject.model.Order;
import uz.pdp.restaurantproject.model.dto.DataDto;

import java.util.List;
import java.util.Optional;

public class OrderRepositoryImpl implements OrderRepositry {

    private static OrderRepositoryImpl instance;

    public static OrderRepositry getInstance() {
        if (instance == null) {
            instance = new OrderRepositoryImpl();
        }
        return instance;
    }

    @Override
    public Optional<Order> findById(String id) {
        return Optional.empty();
    }

    @Override
    public Order save(Order order) {
        EntityManager entityManager = JPAConfig.getEntityManager();

        entityManager.getTransaction().begin();
        if (findById(order.getId()).isPresent()) {
            entityManager.merge(order);
        } else {
            entityManager.persist(order);
        }
        entityManager.getTransaction().commit();
        entityManager.close();
        return order;
    }

    @Override
    public void delete(Order order) {

    }

    @Override
    public DataDto<List<Order>> findAll(BaseCriteria criteria) {
        return null;
    }
}
