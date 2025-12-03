package uz.pdp.restaurantproject.service;

import uz.pdp.restaurantproject.model.*;
import uz.pdp.restaurantproject.model.enums.OrderStatus;
import uz.pdp.restaurantproject.repository.ClientRepository;
import uz.pdp.restaurantproject.repository.OrderRepository;
import uz.pdp.restaurantproject.repository.impl.ClientRepositoryImpl;
import uz.pdp.restaurantproject.repository.impl.OrderRepositoryImpl;
import uz.pdp.restaurantproject.validator.FoodValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderService {
    private static OrderService instance;
    private final OrderRepository repository = OrderRepositoryImpl.getInstance();
    private final FoodValidator foodValidator = FoodValidator.getInstance();
    private final ClientRepository clientRepository = ClientRepositoryImpl.getInstance();
    private final OrderNotificationService notificationService = OrderNotificationService.getInstance();

    private OrderService() {}

    public static OrderService getInstance() {
        if (instance == null) {
            instance = new OrderService();
        }
        return instance;
    }

    public void addToCart(String foodId, String chatId) {
        Client client = clientRepository.findByChatId(chatId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        Food food = foodValidator.existsAndGet(foodId);

        Order cart = getCart(chatId);

        if (cart == null) {
            cart = new Order();
            cart.setClient(client);
            cart.setStatus(OrderStatus.CART);
            cart.setNumber(generateOrderNumber());
            cart.setItems(new ArrayList<>());
            cart.setCreatedAt(LocalDateTime.now());
            cart.setCreatedBy(chatId);
            cart = repository.save(cart);
        }

        OrderItem existingItem = cart.getItems().stream()
                .filter(item -> item.getFood().getId().equals(food.getId()))
                .findFirst().orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + 1);
            cart.setUpdatedBy(chatId);
            cart.setUpdatedAt(LocalDateTime.now());
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

    public void increaseQuantity(String foodId, String chatId) {
        changeQuantity(foodId, chatId, 1);
    }

    public void decreaseQuantity(String foodId, String chatId) {
        changeQuantity(foodId, chatId, -1);
    }

    private void changeQuantity(String foodId, String chatId, int helper) {
        Order cart = repository.getCart(chatId);

        if (cart == null) return;

        cart.getItems().stream()
                .filter(item -> item.getFood().getId().equals(foodId))
                .findFirst()
                .ifPresent(item -> {
                    int newQty = item.getQuantity() + helper;

                    if (newQty <= 0) {
                        cart.getItems().remove(item);
                    } else {
                        item.setQuantity(newQty);
                        repository.updateOrderItem(item);
                    }
                });
    }

    public String generateOrderNumber() {
        return "ORDER-" + System.currentTimeMillis();
    }

    public Order getCart(String chatId) {
        return repository.getCart(chatId);
    }

    public List<CartItemDto> getCartItemsDto(String chatId) {
        Order cart = getCart(chatId);

        if (cart == null) return new ArrayList<>();

        return cart.getItems().stream().map(item -> new CartItemDto(
                item.getFood().getId(),
                item.getFood().getName(),
                item.getQuantity(),
                item.getPrice() * item.getQuantity()
        )).toList();
    }

    public List<Order> getUserOrders(String chatId) {
        return repository.findOrdersByClientChatId(chatId);
    }

    public void save(Order cart) {
        repository.save(cart);
    }

    // ИСПРАВЛЕНО: Теперь корректно обрабатываем переход статуса
    public void checkout(String chatId) {
        Order cart = repository.getCart(chatId);

        if (cart == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // ВАЖНО: Если у cart нет createdAt, устанавливаем сейчас
        if (cart.getCreatedAt() == null) {
            cart.setCreatedAt(LocalDateTime.now());
        }

        // Меняем статус текущей корзины на CREATED
        cart.setStatus(OrderStatus.CREATED);
        cart.setUpdatedAt(LocalDateTime.now());
        cart.setUpdatedBy(chatId);
        Order createdOrder = repository.save(cart);

        // Создаём новую пустую корзину для пользователя
        Order newCart = new Order();
        newCart.setClient(cart.getClient());
        newCart.setStatus(OrderStatus.CART);
        newCart.setNumber(generateOrderNumber());
        newCart.setItems(new ArrayList<>());
        newCart.setCreatedAt(LocalDateTime.now());
        newCart.setCreatedBy(chatId);
        repository.save(newCart);

        // Отправляем уведомление о созданном заказе
        // ВАЖНО: используем createdOrder, который уже имеет ID после save()
        notificationService.notifyOrderCreated(createdOrder);
    }

    public List<Order> getAllOrdersExceptCart() {
        return repository.findAll();
    }

    public Order getCartItemsByUserId(String chatId) {
        return repository.getUserOrders(chatId);
    }

    // ИСПРАВЛЕНО: Добавлена проверка null
    public void updateOrderStatus(String orderId, OrderStatus newStatus) {
        if (orderId == null || orderId.isEmpty()) {
            throw new RuntimeException("Order ID cannot be null or empty");
        }

        Order order = getByIdWithDetails(orderId);

        if (order == null) {
            throw new RuntimeException("Order not found: " + orderId);
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        repository.save(order);

        if (shouldNotify(newStatus)) {
            notificationService.notifyStatusChange(order, newStatus);
        }
    }

    private boolean shouldNotify(OrderStatus status) {
        return status == OrderStatus.CONFIRMED ||
                status == OrderStatus.PREPARING ||
                status == OrderStatus.IN_DELIVERY ||
                status == OrderStatus.DELIVERED ||
                status == OrderStatus.CANCELED;
    }

    public Order getByIdWithDetails(String orderId) {
        return repository.getByIdWithDetails(orderId);
    }

    public List<Order> getActiveOrders() {
        return repository.findAll().stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELED &&
                        order.getStatus() != OrderStatus.DELIVERED)
                .toList();
    }

    public List<Order> getHistoryOrders() {
        return repository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.CANCELED || order.getStatus() == OrderStatus.DELIVERED)
                .toList();
    }

}
