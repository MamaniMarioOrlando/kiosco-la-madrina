package devMario.example.kioscoLaMadrina.service;

import devMario.example.kioscoLaMadrina.dto.AuthRequestDTO;
import devMario.example.kioscoLaMadrina.dto.JwtResponseDTO;

public interface AuthService {
    JwtResponseDTO login(AuthRequestDTO request);
}
