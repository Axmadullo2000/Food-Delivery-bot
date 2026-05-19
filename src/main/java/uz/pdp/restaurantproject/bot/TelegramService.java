package uz.pdp.restaurantproject.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.pdp.restaurantproject.model.Client;
import uz.pdp.restaurantproject.model.Order;
import uz.pdp.restaurantproject.model.OrderItem;
import uz.pdp.restaurantproject.model.dto.ClientCreateDto;
import uz.pdp.restaurantproject.model.dto.ClientDto;
import uz.pdp.restaurantproject.model.dto.FoodDto;
import uz.pdp.restaurantproject.model.enums.OrderStatus;
import uz.pdp.restaurantproject.service.ClientService;
import uz.pdp.restaurantproject.service.FoodService;
import uz.pdp.restaurantproject.service.OrderService;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TelegramService {
    private static final Logger log = Logger.getLogger(TelegramService.class.getName());
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final TelegramService INSTANCE = new TelegramService();

    private final TextMaker textMaker = TextMaker.getInstance();
    private final FoodService foodService = FoodService.getInstance();
    private final OrderService orderService = OrderService.getInstance();
    private final ClientService clientService = ClientService.getInstance();
    private final MarkupBoardService markupBoardService = MarkupBoardService.getInstance();

    private TelegramService() {}

    public static TelegramService getInstance() {
        return INSTANCE;
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

    public void sendFoodInfo(String foodId, String chatId) {
        try {
            FoodDto food = foodService.get(foodId);
            String caption = textMaker.prepareFoodInfo(food);
            File imageFile = resolveImage(food.getImageUrl());

            if (imageFile == null) {
                RestaurantBot.getInstance().sendMessage(SendMessage.builder()
                        .chatId(chatId)
                        .text(caption)
                        .replyMarkup(markupBoardService.foodButton(food))
                        .build());
                return;
            }

            RestaurantBot.getInstance().sendPhoto(SendPhoto.builder()
                    .chatId(chatId)
                    .photo(new InputFile(imageFile))
                    .caption(caption)
                    .parseMode("Markdown")
                    .replyMarkup(markupBoardService.foodButton(food))
                    .build());
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in sendFoodInfo", e);
        }
    }

    private static File resolveImage(String imageName) {
        if (imageName == null || imageName.isEmpty()) {
            return null;
        }
        File file = new File(System.getProperty("user.home") + "/uploads/" + imageName);
        return file.exists() ? file : null;
    }

    public void addFoodToCart(String foodId, String chatId, String userName) {
        Optional<Client> optionalClient = clientService.findByChatId(chatId);
        if (optionalClient.isEmpty()) {
            clientService.create(ClientCreateDto.builder()
                    .chatId(chatId)
                    .fullName(userName != null ? userName : "Unknown")
                    .build());
        }
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

        if (cart == null || cart.getItems().isEmpty()) {
            RestaurantBot.getInstance().sendMessage(SendMessage.builder()
                    .chatId(chatId)
                    .text("🛒 Cart is empty")
                    .parseMode("Markdown")
                    .replyMarkup(markupBoardService.mainMenu())
                    .build());
            return;
        }

        StringBuilder sb = new StringBuilder("🛒 *Cart:*\n\n");
        double totalPrice = 0;
        for (OrderItem item : cart.getItems()) {
            double linePrice = item.getPrice() * item.getQuantity();
            totalPrice += linePrice;
            sb.append(String.format("%d × %s — %.0f sum%n",
                    item.getQuantity(),
                    item.getFood().getName(),
                    linePrice));
        }
        sb.append(String.format("%n*Total: %.0f sum*", totalPrice));

        RestaurantBot.getInstance().sendMessage(SendMessage.builder()
                .chatId(chatId)
                .text(sb.toString())
                .parseMode("Markdown")
                .replyMarkup(markupBoardService.cartKeyboard(cart.getItems()))
                .build());
    }

    public void sendMyOrders(String chatId) {
        List<Order> orders = orderService.getUserOrders(chatId);
        String text = orders.isEmpty()
                ? "📦 No orders yet.\n\nMake your first order now and enjoy great food!"
                : renderOrders(orders);

        RestaurantBot.getInstance().sendMessage(SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("Markdown")
                .replyMarkup(markupBoardService.mainMenu())
                .build());
    }

    private static String renderOrders(List<Order> orders) {
        StringBuilder sb = new StringBuilder("📦 *Your Orders:*\n\n");
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            sb.append(String.format("*Order #%d*%n", i + 1));
            sb.append(String.format("📅 %s%n", order.getCreatedAt().format(DATE_TIME)));
            sb.append(String.format("📊 Status: %s%n%n", statusLabel(order.getStatus())));

            double orderTotal = 0;
            for (OrderItem item : order.getItems()) {
                double linePrice = item.getPrice() * item.getQuantity();
                orderTotal += linePrice;
                sb.append(String.format("   • %d × %s — %.0f sum%n",
                        item.getQuantity(),
                        item.getFood().getName(),
                        linePrice));
            }
            sb.append(String.format("%n*Total: %.0f sum*%n", orderTotal));
            sb.append("────────────────────\n\n");
        }
        return sb.toString();
    }

    private static String statusLabel(OrderStatus status) {
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
}
