package uz.pdp.restaurantproject.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import uz.pdp.restaurantproject.config.JPAConfig;
import uz.pdp.restaurantproject.criteria.BaseCriteria;
import uz.pdp.restaurantproject.model.Client;
import uz.pdp.restaurantproject.model.dto.DataDto;
import uz.pdp.restaurantproject.repository.ClientRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        return findByChatId(id);
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
        EntityManager entityManager = JPAConfig.getEntityManager();
        List<Client> all = entityManager.createQuery("SELECT c FROM Client c WHERE c.fullName = :name", Client.class)
                .setParameter("name", criteria)
                .getResultList();

        return new DataDto<>(all, 0);
    }

    @Override
    public List<Client> findAll() {
        return new ArrayList<>(findAll());
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
