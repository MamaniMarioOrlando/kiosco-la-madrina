package devMario.example.kioscoLaMadrina.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Diagnóstico de IPs detrás de proxies (p. ej. Railway): registra qué dirección ve la aplicación después de
 * procesar X-Forwarded-For y qué informan los encabezados del proxy. Sirve para verificar que el rate
 * limiting cuenta por cliente real y no por proxy.
 *
 * Apagado por defecto (DEBUG). Se enciende sin tocar código con LOGGING_LEVEL_KIOSCO_CLIENTIP=DEBUG.
 * El nombre del logger está en minúsculas a propósito: Spring Boot pasa a minúsculas los nombres de
 * logger que vienen de variables de entorno, así que un nombre con mayúsculas no podría encenderse así.
 */
@Component
public class ClientIpLoggingFilter extends OncePerRequestFilter {

    static final String LOGGER_NAME = "kiosco.clientip";

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("{} {} -> remoteAddr={} X-Forwarded-For={} X-Real-IP={}",
                    request.getMethod(), request.getRequestURI(), request.getRemoteAddr(),
                    request.getHeader("X-Forwarded-For"), request.getHeader("X-Real-IP"));
        }
        filterChain.doFilter(request, response);
    }
}
