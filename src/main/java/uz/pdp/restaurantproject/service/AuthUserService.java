package uz.pdp.restaurantproject.service;

import uz.pdp.restaurantproject.repository.impl.AuthUserRepository;

public class AuthUserService {
    private static AuthUserService instance;
    private final AuthUserRepository repository = AuthUserRepository.getInstance();

    public static AuthUserService getInstance() {
        if (instance == null) {
            instance = new AuthUserService();
        }
        return instance;
    }

    public AuthUserRepository getAuthUserRepository() {
        return repository;
    }
}
