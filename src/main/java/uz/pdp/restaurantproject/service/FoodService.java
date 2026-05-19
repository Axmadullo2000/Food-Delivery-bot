package uz.pdp.restaurantproject.service;

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

public final class FoodService extends AbstractService<FoodRepository, FoodMapper, FoodValidator> {
    private static final FoodService INSTANCE = new FoodService();

    private FoodService() {
        super(FoodRepositoryImpl.getInstance(), FoodMapper.getInstance(), FoodValidator.getInstance());
    }

    public static FoodService getInstance() {
        return INSTANCE;
    }

    public DataDto<List<FoodDto>> getAll(BaseCriteria criteria) {
        DataDto<List<Food>> page = repository.findAll(criteria);
        return new DataDto<>(mapper.toDto(page.getData()), page.getTotalPages());
    }

    public List<FoodDto> getAll() {
        return mapper.toDto(repository.findAll());
    }

    public void delete(String id) {
        Food food = validator.existsAndGet(id);
        repository.delete(food);
    }

    public void create(FoodCreateDto dto) {
        repository.save(mapper.fromDto(dto));
    }

    public void update(FoodUpdateDto dto, String id) {
        Food food = validator.existsAndGet(id);
        mapper.fromDto(dto, food);
        repository.save(food);
    }

    public FoodDto get(String foodId) {
        return mapper.toDto(validator.existsAndGet(foodId));
    }
}
