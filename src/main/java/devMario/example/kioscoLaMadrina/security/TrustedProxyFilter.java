package devMario.example.kioscoLaMadrina.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Resuelve la IP real del cliente cuando el pedido llega a través del proxy de Next.js (Vercel).
 *
 * Detrás del proxy, todas las conexiones vienen de servidores de Vercel: sin esto, el rate limiting
 * contaría a todos los usuarios como si fueran uno solo. El proxy informa la IP del navegador en
 * {@value #CLIENT_IP_HEADER}, pero el backend es público y cualquiera podría mandar ese encabezado
 * inventado. Por eso solo se le cree si el pedido trae además el secreto compartido.
 *
 * Corre antes que cualquier otro filtro y "envuelve" el pedido para que getRemoteAddr() devuelva la IP
 * real: el rate limiting, el login y los logs siguen igual, sin saber que existe un proxy (abierto/cerrado).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TrustedProxyFilter extends OncePerRequestFilter {

    public static final String SECRET_HEADER = "X-Kiosco-Proxy-Secret";
    public static final String CLIENT_IP_HEADER = "X-Kiosco-Client-IP";

    /** Igual que la clave JWT: un secreto corto se puede adivinar probando. */
    static final int MIN_SECRET_LENGTH = 32;

    private static final Pattern IPV4 = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3}");
    private static final Pattern IPV6 = Pattern.compile("[0-9a-fA-F:.]+");

    private static final Logger log = LoggerFactory.getLogger(TrustedProxyFilter.class);

    private final byte[] secret;

    /** @param configured PROXY_SECRET; vacía = no hay proxy de confianza (desarrollo local, sin proxy) */
    public TrustedProxyFilter(@Value("${kiosco.proxy.secret:}") String configured) {
        if (!StringUtils.hasText(configured)) {
            this.secret = null;
            log.info("PROXY_SECRET no está definida: no se confía en ningún proxy (la IP es la de la conexión).");
            return;
        }
        if (configured.trim().length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException("PROXY_SECRET debe tener al menos " + MIN_SECRET_LENGTH
                    + " caracteres. Generá uno con: openssl rand -base64 32");
        }
        this.secret = configured.trim().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Optional<String> clientIp = fromTrustedProxy(request) ? ipLiteral(request.getHeader(CLIENT_IP_HEADER))
                : Optional.empty();

        chain.doFilter(clientIp.<HttpServletRequest>map(ip -> new ClientIpRequest(request, ip)).orElse(request),
                response);
    }

    private boolean fromTrustedProxy(HttpServletRequest request) {
        String sent = request.getHeader(SECRET_HEADER);
        // MessageDigest.isEqual compara en tiempo constante: un equals() normal corta en el primer carácter
        // distinto, y midiendo cuánto tarda la respuesta se podría adivinar el secreto letra por letra.
        return secret != null && sent != null
                && MessageDigest.isEqual(secret, sent.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Solo una IP literal (v4 o v6), nunca una lista ni un nombre: el valor termina siendo una clave del
     * rate limiting. Nunca se llama a InetAddress con algo que no tenga forma de IP: con un nombre de host
     * haría una consulta DNS.
     */
    static Optional<String> ipLiteral(String value) {
        if (value == null) {
            return Optional.empty();
        }
        String candidate = value.trim();
        if (IPV4.matcher(candidate).matches()) {
            return isValidIpv4(candidate) ? Optional.of(candidate) : Optional.empty();
        }
        if (candidate.contains(":") && IPV6.matcher(candidate).matches()) {
            try {
                InetAddress.getByName(candidate); // con ":" Java lo trata como literal IPv6: no consulta DNS
                return Optional.of(candidate);
            } catch (UnknownHostException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static boolean isValidIpv4(String candidate) {
        for (String octet : candidate.split("\\.")) {
            if (Integer.parseInt(octet) > 255) {
                return false;
            }
        }
        return true;
    }

    private static final class ClientIpRequest extends HttpServletRequestWrapper {

        private final String clientIp;

        ClientIpRequest(HttpServletRequest request, String clientIp) {
            super(request);
            this.clientIp = clientIp;
        }

        @Override
        public String getRemoteAddr() {
            return clientIp;
        }

        @Override
        public String getRemoteHost() {
            return clientIp;
        }
    }
}
