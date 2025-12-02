package uz.pdp.restaurantproject.model.dto;

import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientDto {
    private String fullName;
    private String phone;
    private String chatId;
    private Double longitude;
    private Double latitude;
}
