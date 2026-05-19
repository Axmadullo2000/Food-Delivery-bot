package uz.pdp.restaurantproject.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class JPAConfig {
    private static final String PERSISTENCE_UNIT = "restaurant_unit";

    private JPAConfig() {}

    /** Holder idiom: the factory is created exactly once, lazily and thread-safely. */
    private static final class Holder {
        private static final EntityManagerFactory FACTORY =
                Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
    }

    public static EntityManager getEntityManager() {
        return Holder.FACTORY.createEntityManager();
    }
}
