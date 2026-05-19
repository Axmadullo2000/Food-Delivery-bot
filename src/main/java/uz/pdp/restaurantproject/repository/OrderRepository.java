package uz.pdp.restaurantproject.repository;

import uz.pdp.restaurantproject.model.Order;
import uz.pdp.restaurantproject.model.OrderItem;

import java.util.List;

public interface OrderRepository extends CrudRepository<Order, String> {

    void createOrderItem(OrderItem newItem);

    void updateOrderItem(OrderItem existingItem);

    void deleteOrderItem(OrderItem item);

    Order getCart(String chatId);

    List<Order> findOrdersByClientChatId(String chatId);

    Order getByIdWithDetails(String orderId);
}
