package uz.pdp.restaurantproject.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import uz.pdp.restaurantproject.config.JPAConfig;
import uz.pdp.restaurantproject.criteria.BaseCriteria;
import uz.pdp.restaurantproject.model.Order;
import uz.pdp.restaurantproject.model.OrderItem;
import uz.pdp.restaurantproject.model.dto.DataDto;
import uz.pdp.restaurantproject.model.enums.OrderStatus;
import uz.pdp.restaurantproject.repository.OrderRepository;

import java.util.List;
import java.util.Optional;

public class OrderRepositoryImpl implements OrderRepository {

    private static OrderRepositoryImpl instance;

    public static OrderRepository getInstance() {
        if (instance == null) {
            instance = new OrderRepositoryImpl();
        }
        return instance;
    }

    @Override
    public Optional<Order> findById(String id) {
        EntityManager entityManager = JPAConfig.getEntityManager();

        try {
            Order order = entityManager.find(Order.class, id);
            return Optional.ofNullable(order);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public Order save(Order order) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Order managedOrder;

            if (order.getId() == null) {
                em.persist(order);
                managedOrder = order;
            } else {
                managedOrder = em.merge(order);
            }

            tx.commit();
            return managedOrder;

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Ошибка сохранения заказа", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Order order) {
        // Реализация при необходимости
    }

    @Override
    public DataDto<List<Order>> findAll(BaseCriteria criteria) {
        EntityManager entityManager = JPAConfig.getEntityManager();
        TypedQuery<Order> query = entityManager.createQuery(
                "SELECT o FROM OrderItem o WHERE o.food.name = :foodName", Order.class);
        List<Order> orders = query.getResultList();

        return new DataDto<>(orders, 1);
    }

    @Override
    public List<Order> findAll() {
        EntityManager em = JPAConfig.getEntityManager();

        try {
            String jpql = """
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.items i
            LEFT JOIN FETCH i.food f
            LEFT JOIN FETCH o.client c
            WHERE o.status != 'CART'
            ORDER BY o.createdAt DESC
            """;

            List<Order> orders = em.createQuery(jpql, Order.class).getResultList();

            // Инициализируем коллекции пока EntityManager открыт
            orders.forEach(o -> {
                if (o.getItems() != null) {
                    o.getItems().size();
                }
                if (o.getClient() != null) {
                    o.getClient().getChatId(); // Инициализируем клиента
                }
            });

            return orders;
        } finally {
            em.close();
        }
    }

    // ИСПРАВЛЕНО: Загружаем ВСЁ сразу через JOIN FETCH
    public Order getByIdWithDetails(String orderId) {
        EntityManager em = JPAConfig.getEntityManager();

        try {
            String jpql = """
                SELECT DISTINCT o FROM Order o
                LEFT JOIN FETCH o.items i
                LEFT JOIN FETCH i.food
                LEFT JOIN FETCH o.client
                WHERE o.id = :id
                """;

            Order order = em.createQuery(jpql, Order.class)
                    .setParameter("id", orderId)
                    .getSingleResult();

            // Инициализируем коллекцию пока EntityManager открыт
            if (order.getItems() != null) {
                order.getItems().size();
            }

            return order;
        } finally {
            em.close();
        }
    }

    public Order findActiveCartByClientChatId(String chatId) {
        EntityManager em = JPAConfig.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT o FROM Order o " +
                                    "LEFT JOIN FETCH o.items " +
                                    "WHERE o.client.chatId = :chatId AND o.status = 'CART'",
                            Order.class)
                    .setParameter("chatId", chatId)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        } finally {
            em.close();
        }
    }

    public Order getCart(String chatId) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            String jpql = """
            SELECT DISTINCT o
            FROM Order o
            JOIN FETCH o.items i
            JOIN FETCH i.food
            JOIN FETCH o.client c
            WHERE c.chatId = :chatId
              AND o.status = 'CART'
            """;

            List<Order> result = em.createQuery(jpql, Order.class)
                    .setParameter("chatId", chatId)
                    .getResultList();

            return result.isEmpty() ? null : result.get(0);

        } finally {
            em.close();
        }
    }

    @Override
    public void updateOrderItem(OrderItem existingItem) {
        EntityManager entityManager = JPAConfig.getEntityManager();

        try {
            entityManager.getTransaction().begin();
            entityManager.merge(existingItem);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw new RuntimeException("Ошибка обновления OrderItem", e);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void createOrderItem(OrderItem newItem) {
        EntityManager entityManager = JPAConfig.getEntityManager();

        try {
            entityManager.getTransaction().begin();
            entityManager.persist(newItem);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw new RuntimeException("Ошибка создания OrderItem", e);
        } finally {
            entityManager.close();
        }
    }

    public Order getUserOrders(String chatId) {
        EntityManager em = JPAConfig.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT o FROM Order o " +
                                    "LEFT JOIN FETCH o.items " +
                                    "WHERE o.client.chatId = :chatId AND o.status = :status",
                            Order.class)
                    .setParameter("chatId", chatId)
                    .setParameter("status", OrderStatus.CART)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Order> findOrdersByClientChatId(String chatId) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            String jpql = """
                SELECT DISTINCT o FROM Order o
                LEFT JOIN FETCH o.items i
                LEFT JOIN FETCH i.food
                WHERE o.client.chatId = :chatId
                AND o.status != :status
                ORDER BY o.createdAt DESC
                """;

            return em.createQuery(jpql, Order.class)
                    .setParameter("chatId", chatId)
                    .setParameter("status", OrderStatus.CART)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}