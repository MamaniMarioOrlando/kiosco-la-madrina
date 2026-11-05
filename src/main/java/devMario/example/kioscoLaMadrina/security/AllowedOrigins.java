package devMario.example.kioscoLaMadrina.security;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Convierte la variable ALLOWED_ORIGINS en la lista de orígenes que CORS acepta.
 *
 * <p>Fail-fast: si el valor es peligroso, está vacío o mal formado, la aplicación no arranca. Es preferible un
 * deploy que falla a la vista en los logs que una API abierta a cualquier sitio, o un frontend que
 * deja de funcionar sin un error que explique por qué.
 */
final class AllowedOrigins {

    private static final Set<String> SCHEMES = Set.of("http", "https");

    private AllowedOrigins() {
    }

    static List<String> parse(String raw) {
        List<String> origins = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();

        if (origins.isEmpty()) {
            throw new IllegalStateException(
                    "ALLOWED_ORIGINS está vacío: indicá al menos el dominio del frontend, p. ej. https://mi-app.vercel.app");
        }
        if (origins.stream().anyMatch(origin -> origin.contains("*"))) {
            throw new IllegalStateException(
                    "ALLOWED_ORIGINS no admite comodines (*): cualquier sitio podría llamar a la API. "
                            + "Listá los dominios exactos separados por coma. Valor recibido: " + raw);
        }
        origins.forEach(AllowedOrigins::requireOriginShape);
        return origins;
    }

    // El header Origin que manda el navegador es siempre esquema://host[:puerto]: sin barra final,
    // sin ruta y sin query. Un valor con otra forma nunca coincidiría con ningún pedido.
    private static void requireOriginShape(String origin) {
        try {
            URI uri = new URI(origin);
            boolean valid = uri.getScheme() != null && SCHEMES.contains(uri.getScheme())
                    && uri.getHost() != null
                    && uri.getUserInfo() == null
                    && (uri.getRawPath() == null || uri.getRawPath().isEmpty())
                    && uri.getRawQuery() == null
                    && uri.getRawFragment() == null;
            if (valid) {
                return;
            }
        } catch (URISyntaxException ignored) {
            // Se informa abajo con el mismo mensaje que cualquier otra forma inválida.
        }
        throw new IllegalStateException("ALLOWED_ORIGINS tiene un origen inválido: \"" + origin
                + "\". Usá la forma esquema://dominio[:puerto], sin barra final ni ruta, "
                + "p. ej. https://mi-app.vercel.app");
    }
}
