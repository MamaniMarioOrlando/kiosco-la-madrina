package devMario.example.kioscoLaMadrina.security.jwt;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * Traduce una {@link InsecureJwtSecretException} al reporte estándar de Spring Boot
 * ("APPLICATION FAILED TO START" con Description y Action) en vez de un stack trace de cientos de líneas.
 *
 * Spring recorre una cadena de FailureAnalyzers hasta que uno reconoce el error (Chain of Responsibility);
 * esta clase agrega un eslabón para nuestro caso. Se registra en META-INF/spring.factories.
 */
public class InsecureJwtSecretFailureAnalyzer extends AbstractFailureAnalyzer<InsecureJwtSecretException> {

    static final String ACTION = """
            Generá una clave nueva con:

                openssl rand -base64 32

            y configurala en la variable de entorno JWT_SECRET_KEY
            (archivo .env en local; pestaña Variables del servicio en Railway).""";

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, InsecureJwtSecretException cause) {
        return new FailureAnalysis(cause.getMessage(), ACTION, cause);
    }
}
