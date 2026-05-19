package uz.pdp.restaurantproject.bot;

/**
 * Telegram bot configuration sourced from environment variables (or JVM system
 * properties as a fallback so values can also be passed via {@code -DTELEGRAM_BOT_TOKEN=...}).
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
    private static final String TOKEN_ENV = "TELEGRAM_BOT_TOKEN";
    private static final String USERNAME_ENV = "TELEGRAM_BOT_USERNAME";

    private static final BotConfig INSTANCE = new BotConfig();

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
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            value = System.getProperty(name);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Environment variable " + name + " is not set. " +
                            "Configure it before starting the application.");
        }
        return value;
    }
}
