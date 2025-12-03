package uz.pdp.restaurantproject.bot;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


@WebListener
public class BotInitializer implements ServletContextListener {
    private Thread thread;

    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("=== Инициализация бота ===");

        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(RestaurantBot.getInstance());

            thread = Thread.currentThread();
            System.out.println("=== Бот успешно зарегистрирован ===");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Остановка бота — прерываем LongPolling...");
                if (thread != null && thread.isAlive()) {
                    thread.interrupt();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
                System.out.println("Бот остановлен чисто.");
            }));


        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Tomcat останавливается — бот будет выключен через shutdown hook.");    }
}
