package uz.pdp.restaurantproject.model.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FoodDto {
    private String id;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private Boolean active;
    private Integer totalAmount;
}
