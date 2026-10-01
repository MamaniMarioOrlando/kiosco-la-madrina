package devMario.example.kioscoLaMadrina.service;

import devMario.example.kioscoLaMadrina.dto.AuthRequestDTO;
import devMario.example.kioscoLaMadrina.dto.JwtResponseDTO;

public interface AuthService {
    /**
     * @param clientIp IP de quien intenta loguearse, para el límite de intentos fallidos
     */
    JwtResponseDTO login(AuthRequestDTO request, String clientIp);
}
