package uz.pdp.restaurantproject.service;

import jakarta.persistence.EntityManager;
import uz.pdp.restaurantproject.config.JPAConfig;
import uz.pdp.restaurantproject.criteria.BaseCriteria;
import uz.pdp.restaurantproject.mapper.FoodMapper;
import uz.pdp.restaurantproject.model.Food;
import uz.pdp.restaurantproject.model.dto.DataDto;
import uz.pdp.restaurantproject.model.dto.FoodCreateDto;
import uz.pdp.restaurantproject.model.dto.FoodDto;
import uz.pdp.restaurantproject.model.dto.FoodUpdateDto;
import uz.pdp.restaurantproject.repository.FoodRepository;
import uz.pdp.restaurantproject.repository.impl.FoodRepositoryImpl;
import uz.pdp.restaurantproject.validator.FoodValidator;

import java.util.List;

public class FoodService extends AbstractService<FoodRepository, FoodMapper, FoodValidator> {

    private static FoodService instance;

    public FoodService() {
        super(FoodRepositoryImpl.getInstance(), FoodMapper.getInstance(), FoodValidator.getInstance());
    }

    public static FoodService getInstance() {
        if (instance == null) {
            instance = new FoodService();
        }

        return instance;
    }

    public DataDto<List<FoodDto>> getAll(BaseCriteria criteria) {
        DataDto<List<Food>> page = repository.findAll(criteria);
        List<FoodDto> foodList = mapper.toDto(page.getData());
        return new DataDto<>(foodList, 0);
    }

    public List<FoodDto> getAll() {
        List<Food> foods = repository.findAll();

        return mapper.toDto(foods);
    }

    public void delete(String id) {
        EntityManager entityManager = JPAConfig.getEntityManager();
        entityManager.getTransaction().begin();
        Food food = entityManager.find(Food.class, id);
        food.setDeleted(true);
        entityManager.merge(food);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    public void create(FoodCreateDto dto) {
        Food food = mapper.fromDto(dto);
        repository.save(food);
    }

    public void update(FoodUpdateDto dto, String id) {
        Food food = validator.existsAndGet(id);
        mapper.fromDto(dto, food);
        repository.save(food);
    }

    public FoodDto get(String foodId) {
        Food food = validator.existsAndGet(foodId);
        return mapper.toDto(food);
    }
}
