package uz.pdp.restaurantproject.service;

import uz.pdp.restaurantproject.model.*;
import uz.pdp.restaurantproject.model.enums.OrderStatus;
import uz.pdp.restaurantproject.repository.ClientRepository;
import uz.pdp.restaurantproject.repository.OrderRepository;
import uz.pdp.restaurantproject.repository.impl.ClientRepositoryImpl;
import uz.pdp.restaurantproject.repository.impl.OrderRepositoryImpl;
import uz.pdp.restaurantproject.validator.FoodValidator;

import java.util.ArrayList;
import java.util.List;


public class OrderService {
    private static OrderService instance;
    private final OrderRepository repository = OrderRepositoryImpl.getInstance();
    private final FoodValidator foodValidator = FoodValidator.getInstance();
    private final ClientRepository clientRepository = ClientRepositoryImpl.getInstance();

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
            cart = repository.save(cart);
        }

        OrderItem exisingItem = cart.getItems().stream()
                .filter(item -> item.getFood().getId().equals(food.getId()))
                .findFirst().orElse(null);

        if (exisingItem != null) {
            exisingItem.setQuantity(exisingItem.getQuantity() + 1);
            repository.updateOrderItem(exisingItem);
        }else {
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
        changeQuantity(foodId,  chatId,-1);
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
                    }else {
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

    public void checkout(String chatId) {
        Order cart = getCart(chatId);

        if (cart != null && !cart.getItems().isEmpty()) {
            cart.setStatus(OrderStatus.CONFIRMED);
            save(cart);
        }
    }

    public List<Order> getAllOrdersExceptCart() {
        return repository.findAll();
    }

    public Order getCartItemsByUserId(String chatId) {
        return repository.getUserOrders(chatId);
    }

    public void updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(newStatus);
        repository.save(order);
    }

}
