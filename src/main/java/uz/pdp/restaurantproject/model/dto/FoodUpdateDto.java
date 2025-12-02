package uz.pdp.restaurantproject.model.dto;

import jakarta.servlet.http.Part;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FoodUpdateDto {
    private String name;
    private String description;
    private Double price;
    private Part image;
    private Boolean active;
    private Integer totalAmount;
}

