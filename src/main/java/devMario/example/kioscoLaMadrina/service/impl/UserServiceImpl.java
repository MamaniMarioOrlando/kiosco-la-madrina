package devMario.example.kioscoLaMadrina.service.impl;

import devMario.example.kioscoLaMadrina.dto.CreateUserRequestDTO;
import devMario.example.kioscoLaMadrina.dto.UserResponseDTO;
import devMario.example.kioscoLaMadrina.exception.BadRequestException;
import devMario.example.kioscoLaMadrina.exception.ConflictException;
import devMario.example.kioscoLaMadrina.exception.ResourceNotFoundException;
import devMario.example.kioscoLaMadrina.mapper.UserMapper;
import devMario.example.kioscoLaMadrina.model.Role;
import devMario.example.kioscoLaMadrina.model.User;
import devMario.example.kioscoLaMadrina.repository.UserRepository;
import devMario.example.kioscoLaMadrina.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .toList();
    }

    @Override
    public UserResponseDTO create(CreateUserRequestDTO request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("El nombre de usuario '" + request.username() + "' ya está en uso");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build();

        return userMapper.toDTO(userRepository.save(user));
    }

    @Override
    public UserResponseDTO updateStatus(Long id, boolean active, String requestedBy) {
        User user = findUserById(id);

        // Evita que un admin se deje afuera a sí mismo (y potencialmente al sistema sin administradores).
        if (!active && user.getUsername().equals(requestedBy)) {
            throw new BadRequestException("No podés desactivar tu propia cuenta");
        }

        user.setActive(active);
        return userMapper.toDTO(user);
    }

    @Override
    public void updateAvatar(String username, String avatarUrl) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario autenticado"));
        user.setAvatarUrl(avatarUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean adminExists() {
        return userRepository.existsByRole(Role.ADMIN);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con id " + id));
    }
}
