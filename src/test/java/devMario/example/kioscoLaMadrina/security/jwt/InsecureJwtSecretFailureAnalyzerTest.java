package devMario.example.kioscoLaMadrina.security.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.diagnostics.FailureAnalysis;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Al arrancar con una clave insegura, el log debe mostrar un reporte claro
 * ("APPLICATION FAILED TO START / Description / Action") en lugar de un stack trace enorme.
 */
class InsecureJwtSecretFailureAnalyzerTest {

    private final InsecureJwtSecretFailureAnalyzer analyzer = new InsecureJwtSecretFailureAnalyzer();

    @Test
    void explainsTheProblemAndHowToFixIt() {
        FailureAnalysis analysis = analyzer.analyze(
                new InsecureJwtSecretException("JWT_SECRET_KEY es una clave de ejemplo pública."));

        assertThat(analysis.getDescription()).isEqualTo("JWT_SECRET_KEY es una clave de ejemplo pública.");
        assertThat(analysis.getAction()).contains("openssl rand -base64 32");
    }

    @Test
    void findsTheCauseEvenWhenSpringWrapsIt() {
        // Spring envuelve la excepción del constructor en varias capas de BeanCreationException.
        Throwable wrapped = new BeanCreationException("jwtKeyProvider",
                new BeanCreationException("jwtUtils", new InsecureJwtSecretException("JWT_SECRET_KEY no está definida.")));

        FailureAnalysis analysis = analyzer.analyze(wrapped);

        assertThat(analysis).isNotNull();
        assertThat(analysis.getDescription()).isEqualTo("JWT_SECRET_KEY no está definida.");
    }

    @Test
    void ignoresUnrelatedFailures() {
        assertThat(analyzer.analyze(new IllegalStateException("otro problema"))).isNull();
    }
}
