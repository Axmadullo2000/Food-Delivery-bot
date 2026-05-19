package uz.pdp.restaurantproject.service;

import uz.pdp.restaurantproject.repository.impl.AuthUserRepository;

public final class AuthUserService {
    private static final AuthUserService INSTANCE = new AuthUserService();

    private final AuthUserRepository repository = AuthUserRepository.getInstance();

    private AuthUserService() {}

    public static AuthUserService getInstance() {
        return INSTANCE;
    }

    public AuthUserRepository getAuthUserRepository() {
        return repository;
    }
}
