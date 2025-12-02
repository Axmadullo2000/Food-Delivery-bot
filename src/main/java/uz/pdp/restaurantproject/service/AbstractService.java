package uz.pdp.restaurantproject.service;

public class AbstractService<R, M, V> {
    protected R repository;
    protected M mapper;
    protected V validator;

    public AbstractService(R repository, M mapper, V validator) {
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
    }
}
