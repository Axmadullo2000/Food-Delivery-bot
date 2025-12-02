package uz.pdp.restaurantproject.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public class MessageHandler {

    private final TelegramService service = TelegramService.getInstance();

    public void handle(Message message) {
        String text = message.getText();
        String chatId = message.getChatId().toString();
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text("unknown")
                .build();

        if (Constants.START.equalsIgnoreCase(text)) {
            service.registerClient(message, chatId);
            service.sendWelcomeMessage(sendMessage);

        }else if (Constants.ICON_MENU.equalsIgnoreCase(text)) {
            service.sendFoodMenu(sendMessage);
        }
    }
}
