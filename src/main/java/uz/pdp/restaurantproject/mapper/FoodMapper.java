package uz.pdp.restaurantproject.mapper;


import uz.pdp.restaurantproject.model.Food;
import uz.pdp.restaurantproject.model.dto.FoodCreateDto;
import uz.pdp.restaurantproject.model.dto.FoodDto;
import uz.pdp.restaurantproject.model.dto.FoodUpdateDto;
import uz.pdp.restaurantproject.service.FileService;

import java.util.List;

public class FoodMapper {

    private static FoodMapper instance;
    private final FileService fileService = FileService.getInstance();

    public static FoodMapper getInstance() {
        if (instance == null) {
            instance = new FoodMapper();
        }
        return instance;
    }

    public List<FoodDto> toDto(List<Food> foods) {
        return foods
                .stream()
                .map(this::toDto)
                .toList();
    }

    public FoodDto toDto(Food food) {
        return FoodDto.builder()
                .id(food.getId())
                .price(food.getPrice())
                .name(food.getName())
                .description(food.getDescription())
                .imageUrl(food.getImage())
                .totalAmount(food.getTotalAmount())
                .active(food.getActive())
                .build();
    }

    public void fromDto(FoodUpdateDto dto, Food food) {
        if (dto.getImage() != null) {
            String url = fileService.upload(dto.getImage());
            food.setImage(url);
        }

        food.setName(dto.getName());
        food.setDescription(dto.getDescription());
        food.setPrice(dto.getPrice());
        food.setActive(dto.getActive());
        food.setTotalAmount(dto.getTotalAmount());

    }

    public Food fromDto(FoodCreateDto dto) {
        String url = fileService.upload(dto.getImage());
        System.out.println("url: " + url);
        Food food = new Food();
        food.setName(dto.getName());
        food.setDescription(dto.getDescription());
        food.setPrice(dto.getPrice());
        food.setActive(dto.getActive());
        food.setTotalAmount(dto.getTotalAmount());
        food.setImage(url);
        food.setDeleted(false);
        return food;

    }
}
