package uz.pdp.restaurantproject.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

public class MessageHandler {

    private final TelegramService service = TelegramService.getInstance();
    private final MarkupBoardService markupBoardService = MarkupBoardService.getInstance();

    public void handle(Message message) {
        String text = message.getText();
        if (text == null) {
            return;
        }
        String chatId = message.getChatId().toString();

        if (Constants.START.equals(text) || Constants.START.equalsIgnoreCase(text)) {
            service.registerClient(message, chatId);
            sendWelcome(message.getFrom(), chatId);
        } else if (Constants.ICON_MENU.equals(text)) {
            sendMenuPrompt(chatId);
        } else if (Constants.CART.equals(text)) {
            service.sendCart(chatId);
        } else if (Constants.MY_ORDERS.equals(text)) {
            service.sendMyOrders(chatId);
        }
    }

    private void sendWelcome(User from, String chatId) {
        String greeting = "Welcome";
        if (from != null) {
            String first = from.getFirstName();
            String last = from.getLastName();
            if (first != null) {
                greeting += " " + first;
            }
            if (last != null) {
                greeting += " " + last;
            }
        }
        SendMessage welcome = SendMessage.builder()
                .chatId(chatId)
                .text(greeting)
                .replyMarkup(markupBoardService.mainMenu())
                .build();
        RestaurantBot.getInstance().sendMessage(welcome);
    }

    private void sendMenuPrompt(String chatId) {
        SendMessage menuMsg = SendMessage.builder()
                .chatId(chatId)
                .text("Choose menu")
                .build();
        service.sendFoodMenu(menuMsg);
    }
}
