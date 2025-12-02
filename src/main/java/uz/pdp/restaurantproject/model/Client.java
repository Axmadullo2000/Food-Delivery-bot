package uz.pdp.restaurantproject.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.pdp.restaurantproject.model.base.AuditableEntity;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "clients")
public class Client extends AuditableEntity {
    private String fullName;
    private String phone;

    @Column(unique = true)
    private String chatId;
    private Double longitude;
    private Double latitude;
}
