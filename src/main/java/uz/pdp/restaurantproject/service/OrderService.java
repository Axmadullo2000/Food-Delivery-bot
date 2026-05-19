package uz.pdp.restaurantproject.service;

import uz.pdp.restaurantproject.model.Client;
import uz.pdp.restaurantproject.model.Food;
import uz.pdp.restaurantproject.model.Order;
import uz.pdp.restaurantproject.model.OrderItem;
import uz.pdp.restaurantproject.model.enums.OrderStatus;
import uz.pdp.restaurantproject.repository.ClientRepository;
import uz.pdp.restaurantproject.repository.OrderRepository;
import uz.pdp.restaurantproject.repository.impl.ClientRepositoryImpl;
import uz.pdp.restaurantproject.repository.impl.OrderRepositoryImpl;
import uz.pdp.restaurantproject.validator.FoodValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class OrderService {
    private static final Set<OrderStatus> NOTIFIABLE_STATUSES = EnumSet.of(
            OrderStatus.CONFIRMED, OrderStatus.PREPARING,
            OrderStatus.IN_DELIVERY, OrderStatus.DELIVERED, OrderStatus.CANCELED);
    private static final Set<OrderStatus> HISTORY_STATUSES = EnumSet.of(
            OrderStatus.DELIVERED, OrderStatus.CANCELED);

    private static final OrderService INSTANCE = new OrderService();

    private final OrderRepository repository = OrderRepositoryImpl.getInstance();
    private final FoodValidator foodValidator = FoodValidator.getInstance();
    private final ClientRepository clientRepository = ClientRepositoryImpl.getInstance();
    private final OrderNotificationService notificationService = OrderNotificationService.getInstance();

    private OrderService() {}

    public static OrderService getInstance() {
        return INSTANCE;
    }

    public void addToCart(String foodId, String chatId) {
        Client client = clientRepository.findByChatId(chatId)
                .orElseThrow(() -> new IllegalStateException("Client not found for chatId=" + chatId));

        Food food = foodValidator.existsAndGet(foodId);
        Order cart = getOrCreateCart(client, chatId);

        OrderItem existingItem = findItem(cart, food.getId());
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + 1);
            touch(cart, chatId);
            repository.updateOrderItem(existingItem);
        } else {
            OrderItem newItem = new OrderItem();
            newItem.setOrder(cart);
            newItem.setFood(food);
            newItem.setPrice(food.getPrice());
            newItem.setQuantity(1);
            cart.getItems().add(newItem);
            repository.createOrderItem(newItem);
        }
    }

    private Order getOrCreateCart(Client client, String chatId) {
        Order cart = repository.getCart(chatId);
        if (cart != null) {
            return cart;
        }
        Order fresh = new Order();
        fresh.setClient(client);
        fresh.setStatus(OrderStatus.CART);
        fresh.setNumber(generateOrderNumber());
        fresh.setItems(new ArrayList<>());
        fresh.setCreatedAt(LocalDateTime.now());
        fresh.setCreatedBy(chatId);
        return repository.save(fresh);
    }

    private static OrderItem findItem(Order cart, String foodId) {
        return cart.getItems().stream()
                .filter(item -> item.getFood().getId().equals(foodId))
                .findFirst()
                .orElse(null);
    }

    private static void touch(Order cart, String chatId) {
        cart.setUpdatedBy(chatId);
        cart.setUpdatedAt(LocalDateTime.now());
    }

    public void increaseQuantity(String foodId, String chatId) {
        changeQuantity(foodId, chatId, +1);
    }

    public void decreaseQuantity(String foodId, String chatId) {
        changeQuantity(foodId, chatId, -1);
    }

    public void removeFromCart(String foodId, String chatId) {
        Order cart = repository.getCart(chatId);
        if (cart == null) return;
        OrderItem item = findItem(cart, foodId);
        if (item != null) {
            cart.getItems().remove(item);
            repository.deleteOrderItem(item);
        }
    }

    public void clearCart(String chatId) {
        Order cart = repository.getCart(chatId);
        if (cart == null || cart.getItems().isEmpty()) {
            return;
        }
        // Iterate over a copy to avoid ConcurrentModificationException.
        for (OrderItem item : new ArrayList<>(cart.getItems())) {
            repository.deleteOrderItem(item);
        }
        cart.getItems().clear();
    }

    private void changeQuantity(String foodId, String chatId, int delta) {
        Order cart = repository.getCart(chatId);
        if (cart == null) return;

        OrderItem item = findItem(cart, foodId);
        if (item == null) return;

        int newQty = item.getQuantity() + delta;
        if (newQty <= 0) {
            cart.getItems().remove(item);
            repository.deleteOrderItem(item);
        } else {
            item.setQuantity(newQty);
            repository.updateOrderItem(item);
        }
    }

    public String generateOrderNumber() {
        return "ORDER-" + System.currentTimeMillis();
    }

    public Order getCart(String chatId) {
        return repository.getCart(chatId);
    }

    public List<Order> getUserOrders(String chatId) {
        return repository.findOrdersByClientChatId(chatId);
    }

    public void save(Order cart) {
        repository.save(cart);
    }

    public void checkout(String chatId) {
        Order cart = repository.getCart(chatId);
        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        if (cart.getCreatedAt() == null) {
            cart.setCreatedAt(LocalDateTime.now());
        }
        cart.setStatus(OrderStatus.CREATED);
        touch(cart, chatId);
        Order createdOrder = repository.save(cart);

        Order newCart = new Order();
        newCart.setClient(cart.getClient());
        newCart.setStatus(OrderStatus.CART);
        newCart.setNumber(generateOrderNumber());
        newCart.setItems(new ArrayList<>());
        newCart.setCreatedAt(LocalDateTime.now());
        newCart.setCreatedBy(chatId);
        repository.save(newCart);

        // Reload with associations so notification rendering doesn't touch detached lazy proxies.
        Order detailed = repository.getByIdWithDetails(createdOrder.getId());
        notificationService.notifyOrderCreated(detailed);
    }

    public void updateOrderStatus(String orderId, OrderStatus newStatus) {
        if (orderId == null || orderId.isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        Order order = repository.getByIdWithDetails(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        repository.save(order);

        if (NOTIFIABLE_STATUSES.contains(newStatus)) {
            notificationService.notifyStatusChange(order, newStatus);
        }
    }

    public Order getByIdWithDetails(String orderId) {
        return repository.getByIdWithDetails(orderId);
    }

    /** Returns active and history orders in a single pass over the underlying list. */
    public Map<Boolean, List<Order>> partitionActiveAndHistory() {
        return repository.findAll().stream()
                .collect(Collectors.partitioningBy(o -> !HISTORY_STATUSES.contains(o.getStatus())));
    }

    public List<Order> getActiveOrders() {
        return partitionActiveAndHistory().get(Boolean.TRUE);
    }

    public List<Order> getHistoryOrders() {
        return partitionActiveAndHistory().get(Boolean.FALSE);
    }
}
