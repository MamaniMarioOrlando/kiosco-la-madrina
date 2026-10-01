package devMario.example.kioscoLaMadrina.service.impl;

import devMario.example.kioscoLaMadrina.dto.AuthRequestDTO;
import devMario.example.kioscoLaMadrina.dto.JwtResponseDTO;
import devMario.example.kioscoLaMadrina.security.jwt.JwtUtils;
import devMario.example.kioscoLaMadrina.security.ratelimit.LoginAttemptGuard;
import devMario.example.kioscoLaMadrina.security.services.UserDetailsImpl;
import devMario.example.kioscoLaMadrina.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final LoginAttemptGuard loginAttemptGuard;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtUtils jwtUtils,
                           LoginAttemptGuard loginAttemptGuard) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.loginAttemptGuard = loginAttemptGuard;
    }

    @Override
    public JwtResponseDTO login(AuthRequestDTO request, String clientIp) {
        // Antes de mirar la contraseña: si se verificara después, un atacante bloqueado
        // seguiría sabiendo cuándo acertó.
        loginAttemptGuard.checkNotBlocked(clientIp, request.username());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (AuthenticationException e) {
            // Credenciales inválidas o cuenta desactivada: cuenta como intento fallido.
            loginAttemptGuard.recordFailure(clientIp, request.username());
            throw e;
        }
        loginAttemptGuard.recordSuccess(clientIp, request.username());

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
