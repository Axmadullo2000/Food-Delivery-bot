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
@Table(name = "foods")
public class Food extends AuditableEntity {

    private String name;

    private String description;

    private Double price;
    private String image; // fileId -> asdkljsadljksadljk

    private Boolean active = true;

    private Integer totalAmount = 0;
}
