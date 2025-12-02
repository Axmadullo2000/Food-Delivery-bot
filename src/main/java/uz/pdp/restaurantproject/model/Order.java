package uz.pdp.restaurantproject.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import uz.pdp.restaurantproject.model.base.AuditableEntity;
import uz.pdp.restaurantproject.model.enums.OrderStatus;

import java.util.ArrayList;
import java.util.List;


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

    @Enumerated(value = EnumType.STRING)
    private OrderStatus status;

    private String number;
    private String receipt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public double getTotal() {
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

}
