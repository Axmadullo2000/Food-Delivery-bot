package uz.pdp.restaurantproject.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.restaurantproject.model.Order;
import uz.pdp.restaurantproject.model.OrderItem;
import uz.pdp.restaurantproject.service.OrderService;

public class CallBackHandler {
    private final TelegramService service = TelegramService.getInstance();
    private final MarkupBoardService markupBoardService = MarkupBoardService.getInstance();

    public void handle(CallbackQuery callbackQuery) {
        String chatId = callbackQuery.getMessage().getChatId().toString();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();

        if (data == null) return;

        try {
            if (data.startsWith("food:")) {
                service.sendFoodInfo(data, chatId);
            } else if (data.startsWith(Constants.ADD_FOOD_TO_CART)) {
                String foodId = data.replace(Constants.ADD_FOOD_TO_CART, "");
                service.addFoodToCart(data, chatId, callbackQuery.getFrom().getUserName());
                service.sendCart(chatId);
            } else if (data.startsWith("inc:")) {
                String foodId = data.substring(4);
                OrderService.getInstance().increaseQuantity(foodId, chatId);
                updateCartMessage(chatId, messageId);
            } else if (data.startsWith("dec:")) {
                String foodId = data.substring(4);
                OrderService.getInstance().decreaseQuantity(foodId, chatId);
                updateCartMessage(chatId, messageId);
            } else if ("clear_cart".equals(data)) {
                Order cart = OrderService.getInstance().getCart(chatId);

                if (cart != null) {
                    cart.getItems().clear();
                    OrderService.getInstance().save(cart);
                }

                updateCartMessage(chatId, messageId);
            } else if ("checkout".equals(data)) {
                // ИСПРАВЛЕНО: передаём chatId, а не orderId
                OrderService.getInstance().checkout(chatId);

                // Удаляем сообщение с корзиной
                DeleteMessage delete = DeleteMessage.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .build();
                RestaurantBot.getInstance().execute(delete);

                // Отправляем подтверждение
                SendMessage sm = SendMessage.builder()
                        .chatId(chatId)
                        .text("✅ Your order has been successfully placed!\n\n" +
                                "We will notify you about the status updates.")
                        .replyMarkup(markupBoardService.mainMenu())
                        .build();
                RestaurantBot.getInstance().sendMessage(sm);
            } else if ("my_orders".equals(data)) {
                service.sendMyOrders(chatId);
            }
        } catch (Exception e) {
            System.err.println("Error in CallBackHandler: " + e.getMessage());
            e.printStackTrace();

            // Отправляем сообщение об ошибке пользователю
            try {
                SendMessage errorMsg = SendMessage.builder()
                        .chatId(chatId)
                        .text("⚠️ An error occurred. Please try again.")
                        .build();
                RestaurantBot.getInstance().sendMessage(errorMsg);
            } catch (Exception ignored) {}
        }
    }

    private void updateCartMessage(String chatId, Integer messageId) throws TelegramApiException {
        Order cart = OrderService.getInstance().getCart(chatId);

        if (cart == null || cart.getItems().isEmpty()) {
            // Удаляем сообщение с корзиной
            DeleteMessage delete = DeleteMessage.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .build();
            RestaurantBot.getInstance().execute(delete);

            // Отправляем новое сообщение с обычным меню
            SendMessage newMsg = SendMessage.builder()
                    .chatId(chatId)
                    .text("🛒 Cart is empty\n\nWhat would you like to order?")
                    .replyMarkup(markupBoardService.mainMenu())
                    .build();
            RestaurantBot.getInstance().sendMessage(newMsg);

            return;
        }

        // Если корзина не пустая — редактируем сообщение
        StringBuilder text = new StringBuilder("🛒 *Your Cart:*\n\n");
        double total = 0;

        for (OrderItem item : cart.getItems()) {
            double linePrice = item.getPrice() * item.getQuantity();
            total += linePrice;
            text.append(String.format("%d × %s — %.0f sum\n",
                    item.getQuantity(),
                    item.getFood().getName(),
                    linePrice));
        }

        text.append(String.format("\n*Total: %.0f sum*", total));

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(text.toString())
                .parseMode("Markdown")
                .replyMarkup(markupBoardService.cartInlineKeyboard(cart.getItems()))
                .build();

        RestaurantBot.getInstance().editMessage(edit);
    }
}
