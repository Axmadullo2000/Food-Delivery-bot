package uz.pdp.restaurantproject.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class RestaurantBot extends TelegramLongPollingBot {
    private static final Logger log = Logger.getLogger(RestaurantBot.class.getName());

    private static final RestaurantBot INSTANCE = new RestaurantBot();

    private final MessageHandler messageHandler = new MessageHandler();
    private final CallBackHandler callBackHandler = new CallBackHandler();

    private RestaurantBot() {
        super(BotConfig.getInstance().getToken());
    }

    public static RestaurantBot getInstance() {
        return INSTANCE;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasCallbackQuery()) {
                callBackHandler.handle(update.getCallbackQuery());
            } else if (update.hasMessage()) {
                messageHandler.handle(update.getMessage());
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to process update", e);
            notifyUserOfError(update);
        }
    }

    private void notifyUserOfError(Update update) {
        if (!update.hasMessage()) {
            return;
        }
        SendMessage errorMsg = SendMessage.builder()
                .chatId(update.getMessage().getChatId().toString())
                .text("⚠️ Произошла ошибка. Попробуйте позже.")
                .build();
        try {
            execute(errorMsg);
        } catch (TelegramApiException ignored) {
            // Best-effort notification — swallow to avoid masking the original failure.
        }
    }

    /** Sends a text message and returns the resulting message id. */
    public int sendMessage(SendMessage message) {
        try {
            return execute(message).getMessageId();
        } catch (TelegramApiException e) {
            log.log(Level.SEVERE, "Failed to send message", e);
            throw new RuntimeException("Could not send message", e);
        }
    }

    public void editMessage(EditMessageText editMessage) {
        try {
            execute(editMessage);
        } catch (TelegramApiException e) {
            log.log(Level.WARNING, "Failed to edit message", e);
        }
    }

    public void sendPhoto(SendPhoto sendPhoto) {
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            log.log(Level.SEVERE, "Failed to send photo", e);
            throw new RuntimeException("Could not send photo", e);
        }
    }

    /**
     * Sends an arbitrary Bot API method and silently logs any failure. Useful for
     * fire-and-forget calls such as {@code DeleteMessage} where the caller does
     * not care about the result.
     */
    public <T extends Serializable, M extends BotApiMethod<T>> void executeSafe(M method) {
        try {
            execute(method);
        } catch (TelegramApiException e) {
            log.log(Level.WARNING, () -> "Telegram call " + method.getMethod() + " failed: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return BotConfig.getInstance().getUsername();
    }
}
