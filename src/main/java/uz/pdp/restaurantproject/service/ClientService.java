package uz.pdp.restaurantproject.service;

import uz.pdp.restaurantproject.model.Client;
import uz.pdp.restaurantproject.model.dto.ClientCreateDto;
import uz.pdp.restaurantproject.model.dto.ClientDto;
import uz.pdp.restaurantproject.repository.ClientRepository;
import uz.pdp.restaurantproject.repository.impl.ClientRepositoryImpl;

import java.util.Optional;

public final class ClientService {
    private static final ClientService INSTANCE = new ClientService();

    private final ClientRepository repository = ClientRepositoryImpl.getInstance();

    private ClientService() {}

    public static ClientService getInstance() {
        return INSTANCE;
    }

    public ClientDto getByChatId(String chatId) {
        return repository.findByChatId(chatId)
                .map(ClientService::toDto)
                .orElse(null);
    }

    public ClientDto create(ClientCreateDto dto) {
        Client client = new Client();
        client.setFullName(dto.getFullName());
        client.setPhone(dto.getPhone());
        client.setChatId(dto.getChatId());
        Client saved = repository.save(client);
        return toDto(saved);
    }

    public Optional<Client> findByChatId(String chatId) {
        return repository.findByChatId(chatId);
    }

    private static ClientDto toDto(Client client) {
        return ClientDto.builder()
                .fullName(client.getFullName())
                .phone(client.getPhone())
                .chatId(client.getChatId())
                .latitude(client.getLatitude())
                .longitude(client.getLongitude())
                .build();
    }
}
