package uz.pdp.restaurantproject.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import uz.pdp.restaurantproject.model.Order;
import uz.pdp.restaurantproject.service.OrderService;

public class CallBackHandler {
    private final TelegramService service = TelegramService.getInstance();
    private final MarkupBoardService markupBoardService = MarkupBoardService.getInstance();

    public void handle(CallbackQuery callbackQuery) {
        String chatId = callbackQuery.getMessage().getChatId().toString();
        String data = callbackQuery.getData();

        if (data == null) return;

        try {
            if (data.startsWith("food:")) {
                service.sendFoodInfo(data, chatId);
            }else if (data.startsWith(Constants.ADD_FOOD_TO_CART)) {
                String foodId = data.replace(Constants.ADD_FOOD_TO_CART, "");
                service.addFoodToCart(data, chatId, callbackQuery.getFrom().getUserName());
                service.sendCart(chatId);
            }else if (data.startsWith("inc:")) {
                String foodId = data.substring(4);
                OrderService.getInstance().increaseQuantity(foodId, chatId);
                service.sendCart(chatId);
            }else if (data.startsWith("dec:")) {
                String foodId = data.substring(4);
                OrderService.getInstance().decreaseQuantity(foodId, chatId);
                service.sendCart(chatId);
            }else if ("clear_cart".equals(data)) {
                Order cart = OrderService.getInstance().getCart(chatId);

                if (cart != null) {
                    cart.getItems().clear();
                    OrderService.getInstance().save(cart);
                }

                service.sendCart(chatId);
            } else if ("checkout".equals(data)) {
                OrderService.getInstance().checkout(chatId);
                SendMessage sm = SendMessage.builder()
                        .chatId(chatId)
                        .text("Your order has been successfully received. Delivery will be completed shortly.")
                        .replyMarkup(markupBoardService.mainMenu())
                        .build();
                RestaurantBot.getInstance().sendMessage(sm);
            }else if ("my_orders".equals(data)) {
                service.sendMyOrders(chatId);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
