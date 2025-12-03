package uz.pdp.restaurantproject.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
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
import java.time.format.DateTimeFormatter;
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
        String welcomeMessage = "Welcome to My restaurant, Girgitton Express!";
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
            System.err.println("Error in sendFoodInfo: " + e.getMessage());
            e.printStackTrace();
        }
    }

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
        Order cart = orderService.getCart(chatId);

        // ИСПРАВЛЕНО: используем правильный тип для клавиатуры
        if (cart == null || cart.getItems().isEmpty()) {
            SendMessage sm = SendMessage.builder()
                    .chatId(chatId)
                    .text("🛒 Cart is empty")
                    .parseMode("Markdown")
                    .replyMarkup(markupBoardService.mainMenu())
                    .build();

            RestaurantBot.getInstance().sendMessage(sm);
        } else {
            StringBuilder sb = new StringBuilder("🛒 *Cart:*\n\n");

            double totalPrice = 0;

            for (OrderItem item : cart.getItems()) {
                double linePrice = item.getPrice() * item.getQuantity();
                totalPrice += linePrice;
                sb.append(String.format("%d × %s — %.0f sum\n",
                        item.getQuantity(),
                        item.getFood().getName(),
                        linePrice));
            }

            sb.append(String.format("\n*Total: %.0f sum*", totalPrice));

            SendMessage sm = SendMessage.builder()
                    .chatId(chatId)
                    .text(sb.toString())
                    .parseMode("Markdown")
                    .replyMarkup(markupBoardService.cartKeyboard(cart.getItems()))
                    .build();

            RestaurantBot.getInstance().sendMessage(sm);
        }
    }

    // ИСПРАВЛЕНО: метод sendMyOrders
    public void sendMyOrders(String chatId) {
        List<Order> orders = orderService.getUserOrders(chatId);

        String text;

        if (orders.isEmpty()) {
            text = "📦 No orders yet.\n\nMake your first order now and enjoy great food!";
        } else {
            StringBuilder sb = new StringBuilder("📦 *Your Orders:*\n\n");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            for (int i = 0; i < orders.size(); i++) {
                Order order = orders.get(i);

                // Заголовок заказа
                sb.append(String.format("*Order #%d*\n", i + 1));
                sb.append(String.format("📅 %s\n", order.getCreatedAt().format(formatter)));
                sb.append(String.format("📊 Status: %s\n\n", getStatusEmoji(order.getStatus())));

                // Позиции заказа
                double orderTotal = 0;

                for (OrderItem item : order.getItems()) {
                    double linePrice = item.getPrice() * item.getQuantity();
                    orderTotal += linePrice;

                    sb.append(String.format("   • %d × %s — %.0f sum\n",
                            item.getQuantity(),
                            item.getFood().getName(),
                            linePrice));
                }

                sb.append(String.format("\n*Total: %.0f sum*\n", orderTotal));
                sb.append("────────────────────\n\n");
            }

            text = sb.toString();
        }

        SendMessage sm = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("Markdown")
                .replyMarkup(markupBoardService.mainMenu())
                .build();

        RestaurantBot.getInstance().sendMessage(sm);
    }

    // Вспомогательный метод для красивого отображения статусов
    private String getStatusEmoji(uz.pdp.restaurantproject.model.enums.OrderStatus status) {
        return switch (status) {
            case CREATED -> "🆕 Created";
            case CONFIRMED -> "✅ Confirmed";
            case PREPARING -> "👨‍🍳 Preparing";
            case IN_DELIVERY -> "🚗 In Delivery";
            case DELIVERED -> "✅ Delivered";
            case CANCELED -> "❌ Canceled";
            case CART -> "🛒 In Cart";
        };
    }

    private void updateCartMessage(String chatId, Integer messageId) {
        Order cart = OrderService.getInstance().getCart(chatId);

        EditMessageText editMessage = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .parseMode("Markdown")
                .build();

        if (cart == null || cart.getItems().isEmpty()) {
            editMessage.setText("🛒 Cart is empty");
            editMessage.setReplyMarkup(null);
        } else {
            StringBuilder text = new StringBuilder("🛒 *Cart:*\n\n");

            double totalPrice = 0;

            for (OrderItem item : cart.getItems()) {
                double linePrice = item.getPrice() * item.getQuantity();
                totalPrice += linePrice;
                text.append(String.format("%d × %s — %.0f sum\n",
                        item.getQuantity(),
                        item.getFood().getName(),
                        linePrice
                ));
            }

            text.append(String.format("\n*Total: %.0f sum*", totalPrice));
            editMessage.setText(text.toString());
            editMessage.setReplyMarkup(markupBoardService.cartInlineKeyboard(cart.getItems()));
        }

        RestaurantBot.getInstance().editMessage(editMessage);
    }
}
