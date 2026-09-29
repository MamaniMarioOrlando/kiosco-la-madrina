package devMario.example.kioscoLaMadrina.service.impl;

import devMario.example.kioscoLaMadrina.dto.AuthRequestDTO;
import devMario.example.kioscoLaMadrina.dto.JwtResponseDTO;
import devMario.example.kioscoLaMadrina.security.jwt.JwtUtils;
import devMario.example.kioscoLaMadrina.security.services.UserDetailsImpl;
import devMario.example.kioscoLaMadrina.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public JwtResponseDTO login(AuthRequestDTO request) {
        // Lanza AuthenticationException si las credenciales no son válidas o la cuenta está desactivada.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new JwtResponseDTO(
                jwtUtils.generateJwtToken(authentication),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                roles);
    }
}
