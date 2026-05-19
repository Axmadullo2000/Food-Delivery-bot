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

public final class MarkupBoardService {
    private static final int MAIN_MENU_COLUMNS = 2;

    private static final MarkupBoardService INSTANCE = new MarkupBoardService();

    private MarkupBoardService() {}

    public static MarkupBoardService getInstance() {
        return INSTANCE;
    }

    public ReplyKeyboard mainMenu() {
        return prepareReplyKeyboard(List.of(
                Constants.ICON_MENU,
                Constants.MY_ORDERS,
                Constants.CART
        ));
    }

    private ReplyKeyboardMarkup prepareReplyKeyboard(List<String> buttonTexts) {
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        for (String text : buttonTexts) {
            row.add(text);
            if (row.size() == MAIN_MENU_COLUMNS) {
                keyboard.add(row);
                row = new KeyboardRow();
            }
        }
        if (!row.isEmpty()) {
            keyboard.add(row);
        }

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(keyboard);
        markup.setResizeKeyboard(true);
        markup.setSelective(true);
        return markup;
    }

    public ReplyKeyboard foods(List<FoodDto> foods) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>(foods.size());
        for (FoodDto food : foods) {
            keyboard.add(List.of(InlineKeyboardButton.builder()
                    .text(food.getName())
                    .callbackData(Constants.FOOD + food.getId())
                    .build()));
        }
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public ReplyKeyboard foodButton(FoodDto food) {
        InlineKeyboardButton add = InlineKeyboardButton.builder()
                .text(Constants.ADD_TO_CART)
                .callbackData(Constants.ADD_FOOD_TO_CART + food.getId())
                .build();
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(List.of(add)))
                .build();
    }

    public InlineKeyboardMarkup cartKeyboard(List<OrderItem> items) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>(items.size() + 1);
        for (OrderItem item : items) {
            String foodId = item.getFood().getId();
            int qty = item.getQuantity();

            InlineKeyboardButton minus = InlineKeyboardButton.builder()
                    .text(qty == 1 ? "Delete" : "-")
                    .callbackData(qty == 1 ? Constants.REMOVE_FROM_CART + foodId
                                           : Constants.DECREMENT + foodId)
                    .build();

            InlineKeyboardButton count = InlineKeyboardButton.builder()
                    .text(qty + " × " + item.getFood().getName())
                    .callbackData(Constants.NOOP)
                    .build();

            InlineKeyboardButton plus = InlineKeyboardButton.builder()
                    .text("+")
                    .callbackData(Constants.INCREMENT + foodId)
                    .build();

            rows.add(List.of(minus, count, plus));
        }

        rows.add(List.of(
                InlineKeyboardButton.builder()
                        .text("Clear")
                        .callbackData(Constants.CLEAR_CART)
                        .build(),
                InlineKeyboardButton.builder()
                        .text("Proceed to Payment")
                        .callbackData(Constants.CHECKOUT)
                        .build()
        ));

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }
}
