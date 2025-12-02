package uz.pdp.restaurantproject.bot;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@WebListener
public class BotInitializer implements ServletContextListener {
    public void contextInitialized(ServletContextEvent sce) {

        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(RestaurantBot.getInstance());

        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }
}
