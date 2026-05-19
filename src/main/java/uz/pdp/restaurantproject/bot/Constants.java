package uz.pdp.restaurantproject.bot;

/**
 * UI strings and callback-data prefixes used by the Telegram bot.
 *
 * <p>Callback payloads have the form {@code "<prefix><id>"} so the colon belongs
 * to the prefix itself. Helper methods make slicing the id back out symmetric
 * with how it was built.</p>
 */
public final class Constants {
    private Constants() {}

    // Reply-keyboard button labels.
    public static final String START = "/start";
    public static final String ICON_MENU = "Menu";
    public static final String MY_ORDERS = "My Orders";
    public static final String CART = "Cart";
    public static final String ADD_TO_CART = "Add to Cart";

    // Callback-data prefixes (must end with ":" when carrying an id).
    public static final String FOOD = "food:";
    public static final String ADD_FOOD_TO_CART = "add_food_to_cart:";
    public static final String INCREMENT = "inc:";
    public static final String DECREMENT = "dec:";
    public static final String REMOVE_FROM_CART = "remove:";

    // Fixed-action callbacks (no id payload).
    public static final String CHECKOUT = "checkout";
    public static final String CLEAR_CART = "clear_cart";
    public static final String MY_ORDERS_ACTION = "my_orders";
    public static final String NOOP = "ignore";

    /**
     * Returns the id portion of a callback payload that starts with {@code prefix},
     * or {@code null} when the payload doesn't match.
     */
    public static String stripPrefix(String data, String prefix) {
        if (data == null || !data.startsWith(prefix)) {
            return null;
        }
        return data.substring(prefix.length());
    }
}
