package uz.pdp.restaurantproject.bot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.pdp.restaurantproject.model.OrderItem;
import uz.pdp.restaurantproject.model.dto.FoodDto;

import java.util.ArrayList;
import java.util.List;

public class MarkupBoardService {
    private static MarkupBoardService instance;

    public static MarkupBoardService getInstance() {
        if (instance == null) {
            instance = new MarkupBoardService();
        }
        return instance;
    }

    public ReplyKeyboard mainMenu() {
        List<String> buttonTexts = List.of(
                Constants.ICON_MENU,
                Constants.MY_ORDERS,
                Constants.CART
        );


        return prepareReplyKeyboard(buttonTexts);
    }

    private ReplyKeyboardMarkup prepareReplyKeyboard(List<String> buttonTexts) {
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        for (int i = 0; i < buttonTexts.size(); i++) {
            row.add(buttonTexts.get(i));
            if ((i + 1) % 2 == 0 || i + 1 == buttonTexts.size()) {
                keyboard.add(row);
                row = new KeyboardRow();
            }
        }

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboard foods(List<FoodDto> foods) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        foods.forEach(food -> {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(food.getName());
            button.setCallbackData("food:" + food.getId());
            row.add(button);
            keyboard.add(row);
        });

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        return markup;

    }

    public ReplyKeyboard foodButton(FoodDto food) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(Constants.ADD_TO_CART);
        button.setCallbackData(Constants.ADD_FOOD_TO_CART + food.getId());
        row.add(button);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(row));

        return markup;
    }

    public ReplyKeyboard cartKeyboard(List<OrderItem> items) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (OrderItem item : items) {
            String foodId = item.getFood().getId();
            int qty = item.getQuantity();

            InlineKeyboardButton minus = InlineKeyboardButton.builder()
                    .text(qty == 1 ? "Delete" : "-")
                    .callbackData(qty == 1 ? "remove:" + foodId : "dec:" + foodId)  // убрать пробелы!
                    .build();

            InlineKeyboardButton count = InlineKeyboardButton.builder()
                    .text(qty + " × " + item.getFood().getName())
                    .callbackData("ignore")
                    .build();

            InlineKeyboardButton plus = InlineKeyboardButton.builder()
                    .text("+")
                    .callbackData("inc:" + foodId)
                    .build();

            rows.add(List.of(minus, count, plus));
        }

        List<InlineKeyboardButton> bottom  = new ArrayList<>();

        bottom.add(InlineKeyboardButton.builder()
                        .text("Clear")
                        .callbackData("clear_cart")
                .build());

        bottom.add(InlineKeyboardButton.builder()
                .text("Proceed to Payment")
                .callbackData("checkout")
                .build());
        rows.add(bottom);

        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }

}
