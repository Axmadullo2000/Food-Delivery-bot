package uz.pdp.restaurantproject.bot;

import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
        super("8476604088:AAFdof23KL9Jrul86hejkgpxeQ4HrAzvc8g");
    }

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
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
        return "@onlayntaklifbot";
    }


}
