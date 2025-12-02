package uz.pdp.restaurantproject.validator;

import uz.pdp.restaurantproject.model.Food;
import uz.pdp.restaurantproject.repository.FoodRepository;
import uz.pdp.restaurantproject.repository.FoodRepositoryImpl;

public class FoodValidator {
    private final FoodRepository repository = FoodRepositoryImpl.getInstance();
    private static FoodValidator instance;

    private FoodValidator() {

    }

    public static FoodValidator getInstance() {
        if (instance == null) {
            instance = new FoodValidator();
        }
        return instance;

    }

    public Food existsAndGet(String id) {
        return repository.findById(id).orElseThrow(
                () -> new RuntimeException("Food with id " + id + " not found!")
        );
    }
}
