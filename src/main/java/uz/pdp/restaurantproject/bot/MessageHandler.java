package uz.pdp.restaurantproject.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public class MessageHandler {

    private final TelegramService service = TelegramService.getInstance();

    public void handle(Message message) {
        String text = message.getText();
        String chatId = message.getChatId().toString();

        if ("/start".equals(text) || Constants.START.equalsIgnoreCase(text)) {
            service.registerClient(message, chatId);
            SendMessage welcome = SendMessage.builder()
                    .chatId(chatId)
                    .text("Welcome " + message.getFrom().getFirstName() + " " + message.getFrom().getLastName())
                    .replyMarkup(MarkupBoardService.getInstance().mainMenu())
                    .build();
            RestaurantBot.getInstance().sendMessage(welcome);

        } else if (Constants.ICON_MENU.equals(text)) {
            SendMessage menuMsg = SendMessage.builder()
                    .chatId(chatId)
                    .text("Choose menu")
                    .build();
            service.sendFoodMenu(menuMsg);

        } else if (Constants.CART.equals(text)) {
            service.sendCart(chatId);

        } else if (Constants.MY_ORDERS.equals(text)) {
            service.sendMyOrders(chatId);
        }
    }

}
