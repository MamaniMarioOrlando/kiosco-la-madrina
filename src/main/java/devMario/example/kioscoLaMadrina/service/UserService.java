package devMario.example.kioscoLaMadrina.service;

import devMario.example.kioscoLaMadrina.dto.CreateUserRequestDTO;
import devMario.example.kioscoLaMadrina.dto.UserResponseDTO;

import java.util.List;

public interface UserService {
    List<UserResponseDTO> findAll();

    UserResponseDTO create(CreateUserRequestDTO request);

    UserResponseDTO updateStatus(Long id, boolean active, String requestedBy);

    void updateAvatar(String username, String avatarUrl);

    boolean adminExists();
}
