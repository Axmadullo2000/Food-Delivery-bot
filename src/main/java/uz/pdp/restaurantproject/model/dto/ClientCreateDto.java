package uz.pdp.restaurantproject.model.dto;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientCreateDto {
    private String fullName;
    private String phone;
    private String chatId;
}
