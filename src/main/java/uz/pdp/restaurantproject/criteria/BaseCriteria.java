package uz.pdp.restaurantproject.criteria;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BaseCriteria {
    private Integer page;
    private Integer size;
    private String search;
}
