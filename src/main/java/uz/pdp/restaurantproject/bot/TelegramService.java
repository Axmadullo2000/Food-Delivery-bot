package uz.pdp.restaurantproject.bot;


import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.pdp.restaurantproject.model.CartItemDto;
import uz.pdp.restaurantproject.model.Client;
import uz.pdp.restaurantproject.model.Order;
import uz.pdp.restaurantproject.model.OrderItem;
import uz.pdp.restaurantproject.model.dto.ClientCreateDto;
import uz.pdp.restaurantproject.model.dto.ClientDto;
import uz.pdp.restaurantproject.model.dto.FoodDto;
import uz.pdp.restaurantproject.service.ClientService;
import uz.pdp.restaurantproject.service.FoodService;
import uz.pdp.restaurantproject.service.OrderService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

public class TelegramService {
    private static TelegramService instance;
    private final TextMaker textMaker = TextMaker.getInstance();
    private final FoodService foodService = FoodService.getInstance();
    private final OrderService orderService = OrderService.getInstance();
    private final ClientService clientService = ClientService.getInstance();
    private final MarkupBoardService markupBoardService = MarkupBoardService.getInstance();


    private TelegramService() {}

    public static TelegramService getInstance() {
        if (instance == null) {
            instance = new TelegramService();
        }
        return instance;
    }

    public void sendWelcomeMessage(SendMessage sendMessage) {
        String welcomeMessage = "Welcome to My restaurant, Girgitton Express !";
        sendMessage.setText(welcomeMessage);
        sendMessage.setReplyMarkup(markupBoardService.mainMenu());
        RestaurantBot.getInstance().sendMessage(sendMessage);
    }

    public void sendFoodMenu(SendMessage sendMessage) {
        List<FoodDto> foods = foodService.getAll();

        if (foods.isEmpty()) {
            sendMessage.setText("Menu is empty");
        } else {
            sendMessage.setText("Choose Menu");
            sendMessage.setReplyMarkup(markupBoardService.foods(foods));
        }

        RestaurantBot.getInstance().sendMessage(sendMessage);
    }

    public void sendFoodInfo(String data, String chatId) {
        try {
            String foodId = data.replace("food:", "");
            FoodDto food = foodService.get(foodId);

            String caption = textMaker.prepareFoodInfo(food);
            String imageName = food.getImageUrl();

            if (imageName != null && !imageName.isEmpty()) {
                String imagePath = System.getProperty("user.home") + "/uploads/" + imageName;
                File imageFile = new File(imagePath);

                if (!imageFile.exists()) {
                    SendMessage sendMessage = SendMessage.builder()
                            .chatId(chatId)
                            .text(caption)
                            .replyMarkup(markupBoardService.foodButton(food))
                            .build();
                    RestaurantBot.getInstance().sendMessage(sendMessage);
                    return;
                }

                SendPhoto sendPhoto = SendPhoto.builder()
                        .chatId(chatId)
                        .photo(new InputFile(imageFile))
                        .caption(caption)
                        .parseMode("Markdown")
                        .replyMarkup(markupBoardService.foodButton(food))
                        .build();

                RestaurantBot.getInstance().sendPhoto(sendPhoto);
            } else {
                SendMessage sendMessage = SendMessage.builder()
                        .chatId(chatId)
                        .text(caption)
                        .replyMarkup(markupBoardService.foodButton(food))
                        .build();
                RestaurantBot.getInstance().sendMessage(sendMessage);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*public void addFoodToCart(String data, String chatId) {
        String foodId = data.replace(Constants.ADD_FOOD_TO_CART, "");
        orderService.addToCart(foodId, chatId);
    }*/

    public void addFoodToCart(String data, String chatId, String userName) {
        // Проверяем клиента
        Optional<Client> optionalClient = clientService.findByChatId(chatId);

        if (optionalClient.isEmpty()) {
            // Создаём нового клиента
            clientService.create(ClientCreateDto.builder()
                    .chatId(chatId)
                    .fullName(userName != null ? userName : "Unknown")
                    .build());
        }

        // После этого клиента точно можно использовать
        String foodId = data.replace(Constants.ADD_FOOD_TO_CART, "");
        orderService.addToCart(foodId, chatId);
    }


    public void registerClient(Message message, String chatId) {
        ClientDto client = clientService.getByChatId(chatId);
        if (client == null) {
            clientService.create(ClientCreateDto.builder()
                    .fullName(message.getFrom().getFirstName())
                    .chatId(chatId)
                    .build());
        }
    }

    public void sendCart(String chatId) {
        String cartText = buildCartText(chatId);
        Order cart = orderService.getCart(chatId);

        SendMessage sm = SendMessage.builder()
                .chatId(chatId)
                .parseMode("Markdown")
                .text(cartText)
                .build();

        if (cart == null || cart.getItems().isEmpty()) {
            sm.setText("Cart is empty");
            sm.setReplyMarkup(markupBoardService.mainMenu());
        } else {
            StringBuilder text = new StringBuilder("Cart:\n\n");

            double totalPrice = 0;

            for (OrderItem item : cart.getItems()) {
                double linePrice = item.getPrice() * item.getQuantity();
                totalPrice += linePrice;
                text.append("%d × %s — %.0f sum\n".formatted(item.getQuantity(), item.getFood().getName(), linePrice));
            }

            text.append("\nTotal price: %.0f sum".formatted(totalPrice));
            sm.setText(text.toString());
            sm.setReplyMarkup(markupBoardService.cartKeyboard(cart.getItems()));
        }

        RestaurantBot.getInstance().sendMessage(sm);
    }

    private String buildCartText(String chatId) {
        Order cart = orderService.getCartItemsByUserId(chatId);

        if (cart == null || cart.getItems().isEmpty()) {
            return "🛒 Your cart is empty";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🛒 *Your Cart:*\n\n");

        double total = 0;

        List<OrderItem> items = cart.getItems();

        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);

            double itemTotal = item.getQuantity() * item.getPrice();
            total += itemTotal;

            sb.append(i + 1)
                    .append(". ")
                    .append(item.getFood().getName())
                    .append(" x ")
                    .append(item.getQuantity())
                    .append(itemTotal)
                    .append("sum\n");
        }

        sb.append("\n*Total:* ").append(total).append(" sum");

        return sb.toString();
    }

    public void sendMyOrders (String chatId){
        List<Order> orders = orderService.getUserOrders(chatId);

        SendMessage sm = SendMessage.builder()
                .chatId(chatId)
                .build();

        if (orders.isEmpty()) {
            sm.setText("No orders yet. Make your first one now and enjoy great food.");
            sm.setReplyMarkup(markupBoardService.mainMenu());
        } else {
            StringBuilder sb = new StringBuilder("**Orders:\n\n");

            for (int i = 0; i < orders.size(); i++) {
                Order order = orders.get(i);

                sb.append("%d. %s\n".formatted(i + 1, order.getCreatedAt()));

                double orderTotal = 0;

                for (OrderItem item : order.getItems()) {
                    double line = item.getPrice() * item.getQuantity();
                    orderTotal += line;

                    sb.append("   • %d × %s — %.0f sum\n"
                            .formatted(item.getQuantity(), item.getFood().getName(), line));
                }

                sb.append("\n*Total price: %.0f sum*\n\n".formatted(orderTotal));
            }

            sm.setText(sb.toString());
            sm.setParseMode("Markdown");
            sm.setReplyMarkup(markupBoardService.mainMenu());
        }

        RestaurantBot.getInstance().sendMessage(sm);
    }
}