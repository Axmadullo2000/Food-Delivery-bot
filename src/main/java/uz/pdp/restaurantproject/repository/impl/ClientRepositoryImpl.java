package uz.pdp.restaurantproject.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import uz.pdp.restaurantproject.config.JPAConfig;
import uz.pdp.restaurantproject.criteria.BaseCriteria;
import uz.pdp.restaurantproject.model.Client;
import uz.pdp.restaurantproject.model.dto.DataDto;
import uz.pdp.restaurantproject.repository.ClientRepository;

import java.util.List;
import java.util.Optional;

public final class ClientRepositoryImpl implements ClientRepository {

    private static final ClientRepositoryImpl INSTANCE = new ClientRepositoryImpl();

    private ClientRepositoryImpl() {}

    public static ClientRepositoryImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<Client> findById(String id) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Client.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public Client save(Client client) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            Client managed;
            if (client.getId() != null && em.find(Client.class, client.getId()) != null) {
                managed = em.merge(client);
            } else {
                em.persist(client);
                managed = client;
            }
            em.getTransaction().commit();
            return managed;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Client client) {
        // Soft-delete via flag, preserved for future use.
    }

    @Override
    public DataDto<List<Client>> findAll(BaseCriteria criteria) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            String search = criteria == null || criteria.getSearch() == null
                    ? ""
                    : criteria.getSearch().toLowerCase().trim();
            boolean hasSearch = !search.isEmpty();

            String jpql = "SELECT c FROM Client c WHERE c.deleted = false"
                    + (hasSearch ? " AND LOWER(c.fullName) LIKE :search" : "");

            TypedQuery<Client> query = em.createQuery(jpql, Client.class);
            if (hasSearch) {
                query.setParameter("search", "%" + search + "%");
            }
            return new DataDto<>(query.getResultList(), 0);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Client> findAll() {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT c FROM Client c WHERE c.deleted = false",
                            Client.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Client> findByChatId(String chatId) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            List<Client> clients = em.createQuery(
                            "SELECT c FROM Client c WHERE c.deleted = false AND c.chatId = :chatId",
                            Client.class)
                    .setParameter("chatId", chatId)
                    .setMaxResults(1)
                    .getResultList();
            return clients.isEmpty() ? Optional.empty() : Optional.of(clients.get(0));
        } finally {
            em.close();
        }
    }
}
