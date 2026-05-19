package uz.pdp.restaurantproject.validator;

import uz.pdp.restaurantproject.model.Food;
import uz.pdp.restaurantproject.repository.FoodRepository;
import uz.pdp.restaurantproject.repository.impl.FoodRepositoryImpl;

public final class FoodValidator {
    private static final FoodValidator INSTANCE = new FoodValidator();

    private final FoodRepository repository = FoodRepositoryImpl.getInstance();

    private FoodValidator() {}

    public static FoodValidator getInstance() {
        return INSTANCE;
    }

    public Food existsAndGet(String id) {
        return repository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Food with id " + id + " not found"));
    }
}
