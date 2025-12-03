package uz.pdp.restaurantproject.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class RestaurantBot extends TelegramLongPollingBot {
    private static RestaurantBot instance;
    private final MessageHandler messageHandler = new MessageHandler();
    private final CallBackHandler callBackHandler = new CallBackHandler();

    public static synchronized RestaurantBot getInstance() {
        if (instance == null) {
            instance = new RestaurantBot();
        }
        return instance;
    }

    // ИСПРАВЛЕНО: Токен берётся из конфига
    public RestaurantBot() {
        super(BotConfig.getInstance().getToken());
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println("=== Получен Update ===");
        System.out.println("Has message: " + update.hasMessage());
        System.out.println("Has callback: " + update.hasCallbackQuery());

        if (update.hasMessage()) {
            System.out.println("Message text: " + update.getMessage().getText());
        }

        try {
            if (update.hasCallbackQuery()) {
                System.out.println("Callback data: " + update.getCallbackQuery().getData());
                callBackHandler.handle(update.getCallbackQuery());
            } else if (update.hasMessage()) {
                messageHandler.handle(update.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Ошибка обработки update: " + e.getMessage());
            e.printStackTrace();

            // Отправляем сообщение об ошибке пользователю
            if (update.hasMessage()) {
                SendMessage errorMsg = SendMessage.builder()
                        .chatId(update.getMessage().getChatId().toString())
                        .text("⚠️ Произошла ошибка. Попробуйте позже.")
                        .build();
                sendMessage(errorMsg);
            }
        }
    }

    public int sendMessage(SendMessage message) {
        try {
            return execute(message).getMessageId();
        } catch (TelegramApiException e) {
            System.err.println("Ошибка отправки сообщения: " + e.getMessage());
            throw new RuntimeException("Не удалось отправить сообщение", e);
        }
    }

    public void editMessage(EditMessageText editMessage) {
        try {
            execute(editMessage);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка редактирования сообщения: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return BotConfig.getInstance().getUsername();
    }

    public void sendPhoto(SendPhoto sendPhoto) {
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка отправки фото: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Не удалось отправить фото", e);
        }
    }
}