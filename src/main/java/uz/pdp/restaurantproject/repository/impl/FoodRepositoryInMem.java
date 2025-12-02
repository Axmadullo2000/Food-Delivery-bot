package uz.pdp.restaurantproject.repository.impl;

import uz.pdp.restaurantproject.criteria.BaseCriteria;
import uz.pdp.restaurantproject.model.Food;
import uz.pdp.restaurantproject.model.dto.DataDto;
import uz.pdp.restaurantproject.repository.FoodRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FoodRepositoryInMem implements FoodRepository {
    private static FoodRepositoryInMem instance;

    public static FoodRepositoryInMem getInstance() {
        if (instance == null) {
            instance = new FoodRepositoryInMem();
        }
        return instance;
    }

    private final List<Food> foods = new ArrayList<>();

    @Override
    public Optional<Food> findById(String id) {
        return foods.stream().filter(food -> food.getId().equals(id)).findFirst();
    }

    @Override
    public Food save(Food food) {
        if (findById(food.getId()).isPresent()) {
            delete(food);
        }

        foods.add(food);
        return food;
    }

    @Override
    public void delete(Food food) {
        foods.remove(food);
    }

    public DataDto<List<Food>> findAll(BaseCriteria criteria) {
        return null;
    }

    @Override
    public List<Food> findAll() {
        return List.of();
    }
}
