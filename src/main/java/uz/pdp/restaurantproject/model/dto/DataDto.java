package uz.pdp.restaurantproject.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataDto<D> {
    private D data;
    private Integer totalPages;

    public DataDto(D data, Integer totalPages) {
        this.data = data;
        this.totalPages = totalPages;
    }
}
