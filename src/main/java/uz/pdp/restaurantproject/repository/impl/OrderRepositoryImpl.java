package uz.pdp.restaurantproject.repository.impl;

import jakarta.persistence.EntityManager;
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
        }finally {
            entityManager.close();
        }
    }

    @Override
    public Order save(Order order) {
        EntityManager entityManager = JPAConfig.getEntityManager();

        try {
            entityManager.getTransaction().begin();

            if (order.getId() != null && entityManager.find(Order.class, order.getId()) != null) {
                order = entityManager.merge(order);
            } else {
                entityManager.persist(order);
            }

            entityManager.getTransaction().commit();
            return order;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally {
            entityManager.close();
        }
    }

    @Override
    public void delete(Order order) {

    }

    @Override
    public DataDto<List<Order>> findAll(BaseCriteria criteria) {
        EntityManager entityManager = JPAConfig.getEntityManager();
        TypedQuery<Order> query = entityManager.createQuery("SELECT o FROM OrderItem o WHERE o.food.name = :foodName", Order.class);
        List<Order> orders = query.getResultList();

        DataDto<List<Order>> dto = new DataDto<>(orders, 1);
        return dto;
    }

    @Override
    public List<Order> findAll() {
        EntityManager entityManager = JPAConfig.getEntityManager();
        TypedQuery<Order> query = entityManager.createQuery("SELECT o FROM Order o", Order.class);
        return query.getResultList();

    }

    public Order findActiveCartByClientChatId(String chatId) {
        EntityManager em = JPAConfig.getEntityManager();

        try {
            return em.createQuery("SELECT o FROM Order o LEFT JOIN o.items WHERE o.client.chatId = :chatId AND o.status = 'CART'", Order.class)
                    .setParameter("chatId", chatId).getSingleResult();
        }catch (Exception e) {
            return null;
        }finally {
            em.close();
        }
    }

    // Upon request
    public Order getCart(String chatId) {
        EntityManager em = JPAConfig.getEntityManager();
        Order activeOrder = findActiveCartByClientChatId(chatId);

        if (activeOrder == null) return null;

        List<OrderItem> items = em.createQuery("SELECT i FROM OrderItem i WHERE i.order.id = :orderId", OrderItem.class)
                .setParameter("orderId", activeOrder.getId()).getResultList();

        activeOrder.setItems(items);
        return activeOrder;
    }

    @Override
    public void updateOrderItem(OrderItem exisingItem) {
        EntityManager entityManager = JPAConfig.getEntityManager();

        try {
            entityManager.getTransaction().begin();
            entityManager.merge(exisingItem);
            entityManager.getTransaction().commit();
        }catch (Exception e) {
            entityManager.getTransaction().rollback();
        }finally {
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
        }catch (Exception e) {
            entityManager.getTransaction().rollback();
        }finally {
            entityManager.close();
        }
    }

    public Order getUserOrders(String chatId) {
        EntityManager em = JPAConfig.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.client.chatId = :chatId AND o.status = :status",
                            Order.class)
                    .setParameter("chatId", chatId)
                    .setParameter("status", OrderStatus.CART)
                    .getSingleResult();
        }catch (Exception e) {
            return null;
        }
        finally {
            em.close();
        }
    }

    @Override
    public List<Order> findOrdersByClientChatId(String chatId) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.client.chatId = :chatId AND o.status != :status ORDER BY o.createdAt DESC",
                            Order.class)
                    .setParameter("chatId", chatId)
                    .setParameter("status", OrderStatus.CART)
                    .getResultList();
        } finally {
            em.close();
        }
    }

}
