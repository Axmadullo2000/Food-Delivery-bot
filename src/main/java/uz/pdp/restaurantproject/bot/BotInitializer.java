package uz.pdp.restaurantproject.bot;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class BotInitializer implements ServletContextListener {
    private static final Logger log = Logger.getLogger(BotInitializer.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("Registering Telegram bot...");
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(RestaurantBot.getInstance());
            log.info("Telegram bot registered successfully.");
        } catch (TelegramApiException e) {
            log.log(Level.SEVERE, "Failed to register Telegram bot", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Servlet container stopping — Telegram long-polling session will close with the JVM.");
    }
}
