package uz.pdp.restaurantproject.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.pdp.restaurantproject.model.dto.FoodDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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


        return prepareReplyKeyboard(buttonTexts, 2);
    }

    private ReplyKeyboardMarkup prepareReplyKeyboard(List<String> buttonTexts, int buttonsPerRow) {
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        for (int i = 0; i < buttonTexts.size(); i++) {
            row.add(buttonTexts.get(i));
            if ((i + 1) % buttonsPerRow == 0 || i + 1 == buttonTexts.size()) {
                keyboard.add(row);
                row = new KeyboardRow(); // Start a new row
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
}
