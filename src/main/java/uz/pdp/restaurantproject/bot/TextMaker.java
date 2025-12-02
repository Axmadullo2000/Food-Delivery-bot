package uz.pdp.restaurantproject.bot;

import uz.pdp.restaurantproject.model.dto.FoodDto;

public class TextMaker {
    private static TextMaker instance;

    public static TextMaker getInstance() {
        if (instance == null) {
            instance = new TextMaker();
        }
        return instance;
    }

    public String prepareFoodInfo(FoodDto food) {

        StringBuilder foodInfo = new StringBuilder();
        foodInfo.append("Name: " + food.getName())
                .append("\nPrice: " + food.getPrice())
                .append("\nDescription: " + food.getDescription());

        return foodInfo.toString();
    }
}
