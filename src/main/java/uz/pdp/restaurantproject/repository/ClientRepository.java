package uz.pdp.restaurantproject.repository;

import uz.pdp.restaurantproject.model.Client;

import java.util.Optional;

public interface ClientRepository extends CrudRepository<Client, String> {
    Optional<Client> findByChatId(String chatId);
}
