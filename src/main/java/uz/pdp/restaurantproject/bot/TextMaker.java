package uz.pdp.restaurantproject.bot;

import uz.pdp.restaurantproject.model.dto.FoodDto;

public final class TextMaker {
    private static final TextMaker INSTANCE = new TextMaker();

    private TextMaker() {}

    public static TextMaker getInstance() {
        return INSTANCE;
    }

    public String prepareFoodInfo(FoodDto food) {
        return new StringBuilder()
                .append("Name: ").append(food.getName())
                .append("\nPrice: ").append(food.getPrice())
                .append("\nDescription: ").append(food.getDescription())
                .toString();
    }
}
