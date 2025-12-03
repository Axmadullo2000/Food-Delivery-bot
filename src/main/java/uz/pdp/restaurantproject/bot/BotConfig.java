package uz.pdp.restaurantproject.bot;

public class BotConfig {
    private static BotConfig instance;

    public static BotConfig getInstance() {
        if (instance == null) {
            instance = new BotConfig();
        }

        return instance;
    }

    public String getToken() {
        return "8573929670:AAH8KL4EeO4w_audUKGiw8Uv9liBxGKlWSg";
    }

    public String getUsername() {
        return "@girgitton_express_bot";
    }

}
