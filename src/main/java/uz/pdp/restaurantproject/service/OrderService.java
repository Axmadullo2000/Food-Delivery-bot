package uz.pdp.restaurantproject.service;

import uz.pdp.restaurantproject.model.Client;
import uz.pdp.restaurantproject.model.Food;
import uz.pdp.restaurantproject.model.Order;
import uz.pdp.restaurantproject.model.enums.OrderStatus;
import uz.pdp.restaurantproject.repository.ClientRepository;
import uz.pdp.restaurantproject.repository.ClientRepositoryImpl;
import uz.pdp.restaurantproject.repository.OrderRepositoryImpl;
import uz.pdp.restaurantproject.repository.OrderRepositry;
import uz.pdp.restaurantproject.validator.FoodValidator;

public class OrderService {
    private static OrderService instance;
    private final OrderRepositry repository = OrderRepositoryImpl.getInstance();
    private final FoodValidator foodValidator = FoodValidator.getInstance();
    private final ClientRepository clientRepository = ClientRepositoryImpl.getInstance();

    public static OrderService getInstance() {
        if (instance == null) {
            instance = new OrderService();
        }
        return instance;
    }

    public void create(String foodId, String chatId) {
        Client client = clientRepository.findByChatId(chatId).orElse(null);
        Food food = foodValidator.existsAndGet(foodId);
        Order order = new Order();
        order.setFood(food);
        order.setStatus(OrderStatus.IN_CART);
        order.setAmount(1);
        order.setClient(client);
        repository.save(order);
    }


}
