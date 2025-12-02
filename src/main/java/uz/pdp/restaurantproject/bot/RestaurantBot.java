package uz.pdp.restaurantproject.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class RestaurantBot extends TelegramLongPollingBot {
    private static RestaurantBot instance;
    private final MessageHandler messageHandler = new MessageHandler();
    private final CallBackHandler callBackHandler = new CallBackHandler();

    public static RestaurantBot getInstance() {
        if (instance == null) {
            instance = new RestaurantBot();
        }
        return instance;
    }

    public RestaurantBot() {
        super("8573929670:AAH8KL4EeO4w_audUKGiw8Uv9liBxGKlWSg");
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println("=== Получен Update ===");
        System.out.println("Has message: " + update.hasMessage());
        System.out.println("Has callback: " + update.hasCallbackQuery());

        if (update.hasMessage()) {
            System.out.println("Message text: " + update.getMessage().getText());
        }

        if (update.hasCallbackQuery()) {
            System.out.println("Callback data: " + update.getCallbackQuery().getData());
            callBackHandler.handle(update.getCallbackQuery());
        } else if (update.hasMessage()) {
            messageHandler.handle(update.getMessage());
        }
    }

    public int sendMessage(SendMessage message) {
        try {
           return execute(message).getMessageId();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String getBotUsername() {
        return "@girgitton_express_bot";
    }

    public void sendPhoto(SendPhoto sendPhoto) {
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
