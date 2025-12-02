package uz.pdp.restaurantproject.bot;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public class CallBackHandler {
    private final TelegramService service = TelegramService.getInstance();

    public void handle(CallbackQuery callbackQuery) {
        String chatId = callbackQuery.getMessage().getChatId().toString();
        String data = callbackQuery.getData();


        if (data.startsWith("food:")) {
            service.sendFoodInfo(data, chatId);

        } else if (data.startsWith(Constants.ADD_FOOD_TO_CART)) {
            service.addFoodToCart(data, chatId);
        }
    }
}
