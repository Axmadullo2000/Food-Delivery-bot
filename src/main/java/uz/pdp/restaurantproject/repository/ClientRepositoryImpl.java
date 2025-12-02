package uz.pdp.restaurantproject.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import uz.pdp.restaurantproject.config.JPAConfig;
import uz.pdp.restaurantproject.criteria.BaseCriteria;
import uz.pdp.restaurantproject.model.Client;
import uz.pdp.restaurantproject.model.dto.DataDto;

import java.util.List;
import java.util.Optional;

public class ClientRepositoryImpl implements ClientRepository {

    private static ClientRepositoryImpl instance;

    public static ClientRepositoryImpl getInstance() {
        if (instance == null) {
            instance = new ClientRepositoryImpl();
        }
        return instance;
    }

    @Override
    public Optional<Client> findById(String id) {
        return Optional.empty();
    }

    @Override
    public Client save(Client client) {
        EntityManager entityManager = JPAConfig.getEntityManager();
        entityManager.getTransaction().begin();

        if (findById(client.getId()).isPresent()) {
            entityManager.merge(client);
        }else {
            entityManager.persist(client);
        }
        entityManager.getTransaction().commit();
        entityManager.close();
        return client;
    }

    @Override
    public void delete(Client client) {

    }

    @Override
    public DataDto<List<Client>> findAll(BaseCriteria criteria) {
        return null;
    }

    @Override
    public Optional<Client> findByChatId(String chatId) {
        EntityManager entityManager = JPAConfig.getEntityManager();
        TypedQuery<Client> query = entityManager
                .createQuery(
                        "select c from Client c where not c.deleted and c.chatId = :chatId"
                        , Client.class)
                .setParameter("chatId", chatId);

        List<Client> clients = query.getResultList();
        entityManager.close();

        if (clients.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(clients.get(0));
    }
}
