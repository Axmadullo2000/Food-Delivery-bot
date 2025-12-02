package uz.pdp.restaurantproject.service;


import uz.pdp.restaurantproject.model.Client;
import uz.pdp.restaurantproject.model.dto.ClientCreateDto;
import uz.pdp.restaurantproject.model.dto.ClientDto;
import uz.pdp.restaurantproject.repository.ClientRepository;
import uz.pdp.restaurantproject.repository.ClientRepositoryImpl;

import java.util.Optional;

public class ClientService {

    private final ClientRepository repository = ClientRepositoryImpl.getInstance();

    private static ClientService instance;

    public static ClientService getInstance() {
        if (instance == null) {
            instance = new ClientService();
        }
        return instance;
    }

    public ClientDto getByChatId(String chatId) {

        Client client = repository.findByChatId(chatId).orElse(null);
        if (client == null) {
            return null;
        }
        return ClientDto.builder()
                .fullName(client.getFullName())
                .phone(client.getPhone())
                .chatId(client.getChatId())
                .latitude(client.getLatitude())
                .longitude(client.getLongitude())
                .build();
    }

    public ClientDto create(ClientCreateDto dto) {
        Client client = new Client();
        client.setFullName(dto.getFullName());
        client.setPhone(dto.getPhone());
        client.setChatId(dto.getChatId());
        repository.save(client);

        return ClientDto.builder()
                .fullName(client.getFullName())
                .phone(client.getPhone())
                .chatId(client.getChatId())
                .latitude(client.getLatitude())
                .longitude(client.getLongitude())
                .build();
    }
}
