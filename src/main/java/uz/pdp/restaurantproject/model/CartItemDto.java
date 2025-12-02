package uz.pdp.restaurantproject.model;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemDto {
    private String id;
    private String name;
    private Integer quantity;
    private Double totalPrice;
}
