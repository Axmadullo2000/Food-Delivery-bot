package uz.pdp.restaurantproject.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import uz.pdp.restaurantproject.model.Order;
import uz.pdp.restaurantproject.model.OrderItem;
import uz.pdp.restaurantproject.service.OrderService;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CallBackHandler {
    private static final Logger log = Logger.getLogger(CallBackHandler.class.getName());

    private final TelegramService service = TelegramService.getInstance();
    private final MarkupBoardService markupBoardService = MarkupBoardService.getInstance();
    private final OrderService orderService = OrderService.getInstance();

    public void handle(CallbackQuery callbackQuery) {
        String chatId = callbackQuery.getMessage().getChatId().toString();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();

        if (data == null) return;

        try {
            dispatch(callbackQuery, chatId, messageId, data);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in CallBackHandler", e);
            sendError(chatId);
        }
    }

    private void dispatch(CallbackQuery callbackQuery, String chatId, Integer messageId, String data) {
        String foodId;
        if ((foodId = Constants.stripPrefix(data, Constants.FOOD)) != null) {
            service.sendFoodInfo(foodId, chatId);
        } else if ((foodId = Constants.stripPrefix(data, Constants.ADD_FOOD_TO_CART)) != null) {
            service.addFoodToCart(foodId, chatId, callbackQuery.getFrom().getUserName());
            service.sendCart(chatId);
        } else if ((foodId = Constants.stripPrefix(data, Constants.INCREMENT)) != null) {
            orderService.increaseQuantity(foodId, chatId);
            updateCartMessage(chatId, messageId);
        } else if ((foodId = Constants.stripPrefix(data, Constants.DECREMENT)) != null) {
            orderService.decreaseQuantity(foodId, chatId);
            updateCartMessage(chatId, messageId);
        } else if ((foodId = Constants.stripPrefix(data, Constants.REMOVE_FROM_CART)) != null) {
            orderService.removeFromCart(foodId, chatId);
            updateCartMessage(chatId, messageId);
        } else if (Constants.CLEAR_CART.equals(data)) {
            orderService.clearCart(chatId);
            updateCartMessage(chatId, messageId);
        } else if (Constants.CHECKOUT.equals(data)) {
            orderService.checkout(chatId);
            deleteMessage(chatId, messageId);
            sendCheckoutConfirmation(chatId);
        } else if (Constants.MY_ORDERS_ACTION.equals(data)) {
            service.sendMyOrders(chatId);
        }
    }

    private void deleteMessage(String chatId, Integer messageId) {
        RestaurantBot.getInstance().executeSafe(DeleteMessage.builder()
                .chatId(chatId)
                .messageId(messageId)
                .build());
    }

    private void sendCheckoutConfirmation(String chatId) {
        SendMessage sm = SendMessage.builder()
                .chatId(chatId)
                .text("""
                        ✅ Your order has been successfully placed!

                        We will notify you about the status updates.""")
                .replyMarkup(markupBoardService.mainMenu())
                .build();
        RestaurantBot.getInstance().sendMessage(sm);
    }

    private void sendError(String chatId) {
        try {
            SendMessage errorMsg = SendMessage.builder()
                    .chatId(chatId)
                    .text("⚠️ An error occurred. Please try again.")
                    .build();
            RestaurantBot.getInstance().sendMessage(errorMsg);
        } catch (Exception ignored) {
            // Best-effort.
        }
    }

    private void updateCartMessage(String chatId, Integer messageId) {
        Order cart = orderService.getCart(chatId);

        if (cart == null || cart.getItems().isEmpty()) {
            deleteMessage(chatId, messageId);
            RestaurantBot.getInstance().sendMessage(SendMessage.builder()
                    .chatId(chatId)
                    .text("""
                            🛒 Cart is empty

                            What would you like to order?""")
                    .replyMarkup(markupBoardService.mainMenu())
                    .build());
            return;
        }

        StringBuilder text = new StringBuilder("🛒 *Your Cart:*\n\n");
        double total = 0;
        for (OrderItem item : cart.getItems()) {
            double linePrice = item.getPrice() * item.getQuantity();
            total += linePrice;
            text.append(String.format("%d × %s — %.0f sum%n",
                    item.getQuantity(),
                    item.getFood().getName(),
                    linePrice));
        }
        text.append(String.format("%n*Total: %.0f sum*", total));

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(text.toString())
                .parseMode("Markdown")
                .replyMarkup(markupBoardService.cartKeyboard(cart.getItems()))
                .build();
        RestaurantBot.getInstance().editMessage(edit);
    }
}
