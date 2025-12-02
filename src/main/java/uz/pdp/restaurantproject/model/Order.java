package uz.pdp.restaurantproject.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.pdp.restaurantproject.model.base.AuditableEntity;
import uz.pdp.restaurantproject.model.enums.OrderStatus;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order extends AuditableEntity {

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "food_id")
    private Food food;

    private Integer amount = 0;

    @Enumerated(value = EnumType.STRING)
    private OrderStatus status = OrderStatus.CREATED;

    //    @Column(unique = true, nullable = false)
    private String number;

    private String receipt;
}
