package uz.pdp.restaurantproject.bot;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.logging.Logger;

/**
 * Telegram bot configuration sourced from .env file, environment variables,
 * or JVM system properties (in that order of priority).
 *
 * <p>Required:</p>
 * <ul>
 *   <li>{@code TELEGRAM_BOT_TOKEN} — the token issued by @BotFather.</li>
 *   <li>{@code TELEGRAM_BOT_USERNAME} — the bot's @username (without the leading "@").</li>
 * </ul>
 *
 * <p>The values are resolved once at startup; missing values fail fast with a
 * clear message rather than letting the Telegram client surface a cryptic error
 * later.</p>
 */
public final class BotConfig {
    private static final Logger log = Logger.getLogger(BotConfig.class.getName());

    private static final String TOKEN_ENV = "TELEGRAM_BOT_TOKEN";
    private static final String USERNAME_ENV = "TELEGRAM_BOT_USERNAME";

    // Load dotenv FIRST, before creating INSTANCE
    private static final Dotenv dotenv = loadDotenv();
    private static final BotConfig INSTANCE = new BotConfig();

    private static Dotenv loadDotenv() {
        // Try multiple directories where .env might be located
        String[] directories = {
            ".",                                                          // Current directory
            System.getProperty("catalina.base", ".") + "/bin",           // Tomcat bin
            System.getProperty("catalina.home", ".") + "/bin",           // Tomcat home bin
            System.getProperty("user.dir"),                              // User working directory
        };

        for (String dir : directories) {
            try {
                Dotenv env = Dotenv.configure()
                        .directory(dir)
                        .ignoreIfMissing()
                        .load();
                // Check if the file actually has our variables
                if (env.get("TELEGRAM_BOT_TOKEN") != null) {
                    log.info(".env file loaded from: " + dir);
                    return env;
                }
            } catch (Exception e) {
                log.fine("Could not load .env from " + dir + ": " + e.getMessage());
            }
        }
        log.warning(".env file not found in any expected location");
        return null;
    }

    private final String token;
    private final String username;

    private BotConfig() {
        this.token = require(TOKEN_ENV);
        this.username = require(USERNAME_ENV);
    }

    public static BotConfig getInstance() {
        return INSTANCE;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    private static String require(String name) {
        // 1. Try .env file first
        if (dotenv != null) {
            String value = dotenv.get(name);
            if (value != null && !value.isBlank()) {
                log.info("Loaded " + name + " from .env file");
                return value;
            }
        }

        // 2. Try system environment variable
        String value = System.getenv(name);
        if (value != null && !value.isBlank()) {
            log.info("Loaded " + name + " from system environment");
            return value;
        }

        // 3. Try JVM system property (-D flag)
        value = System.getProperty(name);
        if (value != null && !value.isBlank()) {
            log.info("Loaded " + name + " from JVM system property");
            return value;
        }

        throw new IllegalStateException(
                "Variable " + name + " is not set. " +
                "Configure it in .env file, environment variable, or JVM property (-D" + name + "=...)");
    }
}
