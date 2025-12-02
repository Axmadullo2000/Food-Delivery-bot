package uz.pdp.restaurantproject.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAConfig {
    private static EntityManagerFactory entityManagerFactory;

    public static EntityManager getEntityManager() {
        if (entityManagerFactory == null) {
            entityManagerFactory = Persistence.createEntityManagerFactory("restaurant_unit");
        }

        return entityManagerFactory.createEntityManager();
    }

}
