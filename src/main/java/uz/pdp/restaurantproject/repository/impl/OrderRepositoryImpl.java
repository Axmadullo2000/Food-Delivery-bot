package uz.pdp.restaurantproject.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import uz.pdp.restaurantproject.config.JPAConfig;
import uz.pdp.restaurantproject.criteria.BaseCriteria;
import uz.pdp.restaurantproject.model.Order;
import uz.pdp.restaurantproject.model.OrderItem;
import uz.pdp.restaurantproject.model.dto.DataDto;
import uz.pdp.restaurantproject.model.enums.OrderStatus;
import uz.pdp.restaurantproject.repository.OrderRepository;

import java.util.List;
import java.util.Optional;

public final class OrderRepositoryImpl implements OrderRepository {

    private static final OrderRepositoryImpl INSTANCE = new OrderRepositoryImpl();

    private OrderRepositoryImpl() {}

    public static OrderRepository getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<Order> findById(String id) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Order.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public Order save(Order order) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Order managed = (order.getId() != null && em.find(Order.class, order.getId()) != null)
                    ? em.merge(order)
                    : persistAndReturn(em, order);

            // Touch fetch-joined collections so callers can read them after the EM closes.
            if (managed.getItems() != null) {
                managed.getItems().size();
                managed.getItems().forEach(item -> {
                    if (item.getFood() != null) {
                        item.getFood().getName();
                    }
                });
            }
            if (managed.getClient() != null) {
                managed.getClient().getChatId();
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

    private static Order persistAndReturn(EntityManager em, Order order) {
        em.persist(order);
        return order;
    }

    @Override
    public void delete(Order order) {
        // Hard-delete intentionally unsupported; orders are kept for history.
    }

    @Override
    public DataDto<List<Order>> findAll(BaseCriteria criteria) {
        // Currently unused by the application. Returns all non-cart orders, ignoring criteria.
        return new DataDto<>(findAll(), 0);
    }

    @Override
    public List<Order> findAll() {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            List<Order> orders = em.createQuery("""
                    SELECT DISTINCT o FROM Order o
                    LEFT JOIN FETCH o.items i
                    LEFT JOIN FETCH i.food f
                    LEFT JOIN FETCH o.client c
                    WHERE o.status <> :cart
                    ORDER BY o.createdAt DESC
                    """, Order.class)
                    .setParameter("cart", OrderStatus.CART)
                    .getResultList();
            initializeOrders(orders);
            return orders;
        } finally {
            em.close();
        }
    }

    private static void initializeOrders(List<Order> orders) {
        for (Order order : orders) {
            if (order.getItems() != null) order.getItems().size();
            if (order.getClient() != null) order.getClient().getChatId();
        }
    }

    @Override
    public Order getByIdWithDetails(String orderId) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            Order order = em.createQuery("""
                    SELECT DISTINCT o FROM Order o
                    LEFT JOIN FETCH o.items i
                    LEFT JOIN FETCH i.food
                    LEFT JOIN FETCH o.client
                    WHERE o.id = :id
                    """, Order.class)
                    .setParameter("id", orderId)
                    .getSingleResult();
            if (order.getItems() != null) {
                order.getItems().size();
            }
            return order;
        } finally {
            em.close();
        }
    }

    @Override
    public Order getCart(String chatId) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            List<Order> result = em.createQuery("""
                    SELECT DISTINCT o FROM Order o
                    LEFT JOIN FETCH o.items i
                    LEFT JOIN FETCH i.food
                    LEFT JOIN FETCH o.client c
                    WHERE c.chatId = :chatId AND o.status = :cart
                    """, Order.class)
                    .setParameter("chatId", chatId)
                    .setParameter("cart", OrderStatus.CART)
                    .setMaxResults(1)
                    .getResultList();
            return result.isEmpty() ? null : result.get(0);
        } finally {
            em.close();
        }
    }

    @Override
    public void updateOrderItem(OrderItem existingItem) {
        runInTransaction(em -> em.merge(existingItem));
    }

    @Override
    public void createOrderItem(OrderItem newItem) {
        runInTransaction(em -> em.persist(newItem));
    }

    @Override
    public void deleteOrderItem(OrderItem item) {
        runInTransaction(em -> {
            OrderItem managed = em.contains(item) ? item : em.merge(item);
            em.remove(managed);
        });
    }

    @Override
    public List<Order> findOrdersByClientChatId(String chatId) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            List<Order> orders = em.createQuery("""
                    SELECT DISTINCT o FROM Order o
                    LEFT JOIN FETCH o.items i
                    LEFT JOIN FETCH i.food
                    WHERE o.client.chatId = :chatId AND o.status <> :cart
                    ORDER BY o.createdAt DESC
                    """, Order.class)
                    .setParameter("chatId", chatId)
                    .setParameter("cart", OrderStatus.CART)
                    .getResultList();
            initializeOrders(orders);
            return orders;
        } finally {
            em.close();
        }
    }

    private interface EmOp {
        void apply(EntityManager em);
    }

    private static void runInTransaction(EmOp op) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            op.apply(em);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
