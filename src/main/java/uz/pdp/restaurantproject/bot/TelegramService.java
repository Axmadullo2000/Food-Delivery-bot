package uz.pdp.restaurantproject.bot;


import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.pdp.restaurantproject.model.dto.ClientCreateDto;
import uz.pdp.restaurantproject.model.dto.ClientDto;
import uz.pdp.restaurantproject.model.dto.FoodDto;
import uz.pdp.restaurantproject.service.ClientService;
import uz.pdp.restaurantproject.service.FoodService;
import uz.pdp.restaurantproject.service.OrderService;

import java.util.List;

public class TelegramService {
    private static TelegramService instance;
    private final TextMaker textMaker = TextMaker.getInstance();
    private final FoodService foodService = FoodService.getInstance();
    private final OrderService orderService = OrderService.getInstance();
    private final ClientService clientService = ClientService.getInstance();
    private final MarkupBoardService markupBoardService = MarkupBoardService.getInstance();


    private TelegramService() {

    }

    public static TelegramService getInstance() {
        if (instance == null) {
            instance = new TelegramService();
        }
        return instance;
    }

    public void sendWelcomeMessage(SendMessage sendMessage) {
        String welcomeMessage = "Welcome to My restaurant!";
        sendMessage.setText(welcomeMessage);
        sendMessage.setReplyMarkup(markupBoardService.mainMenu());
        RestaurantBot.getInstance().sendMessage(sendMessage);
    }

    public void sendFoodMenu(SendMessage sendMessage) {
        List<FoodDto> foods = foodService.getAll();

        if (foods.isEmpty()) {
            sendMessage.setText("Menu is empty");
        } else {
            sendMessage.setText("Food Menu");
            sendMessage.setReplyMarkup(markupBoardService.foods(foods));
        }

        RestaurantBot.getInstance().sendMessage(sendMessage);
    }

    public void sendFoodInfo(String data, String chatId) {
        String foodId = data.replace("food:", "");
        FoodDto food = foodService.get(foodId);
        String foodInfo = textMaker.prepareFoodInfo(food);
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(foodInfo)
                .build();

        sendMessage.setReplyMarkup(markupBoardService.foodButton(food));

        RestaurantBot.getInstance().sendMessage(sendMessage);
    }

    public void addFoodToCart(String data, String chatId) {
        String foodId = data.replace(Constants.ADD_FOOD_TO_CART, "");
        orderService.create(foodId, chatId);
    }

    public void registerClient(Message message, String chatId) {
        ClientDto client = clientService.getByChatId(chatId);
        if (client == null) {
            clientService.create(ClientCreateDto.builder()
                    .fullName(message.getFrom().getFirstName())
                    .chatId(chatId)
                    .build());
        }
    }
}
