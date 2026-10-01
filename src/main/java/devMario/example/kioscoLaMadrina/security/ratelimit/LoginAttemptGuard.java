package devMario.example.kioscoLaMadrina.security.ratelimit;

import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Locale;

/**
 * Protege el login contra la fuerza bruta contando los intentos FALLIDOS.
 *
 * - Por cuenta + IP: un empleado que se equivoca no bloquea al resto del kiosco (comparten la IP del local),
 *   y un atacante no puede bloquear una cuenta desde otra red.
 * - Por IP, sumando todas las cuentas: frena a quien prueba una contraseña contra muchos usuarios.
 *
 * Un login exitoso reinicia el contador de esa cuenta.
 */
@Component
public class LoginAttemptGuard {

    private final BucketRegistry failuresPerAccount;
    private final BucketRegistry failuresPerIp;

    public LoginAttemptGuard(@Qualifier("loginFailuresPerAccount") BucketRegistry failuresPerAccount,
                             @Qualifier("loginFailuresPerIp") BucketRegistry failuresPerIp) {
        this.failuresPerAccount = failuresPerAccount;
        this.failuresPerIp = failuresPerIp;
    }

    /** Lanza {@link TooManyLoginAttemptsException} si la cuenta o la IP agotaron sus intentos. */
    public void checkNotBlocked(String clientIp, String username) {
        Duration accountWait = timeUntilNextAttempt(failuresPerAccount.bucketFor(accountKey(clientIp, username)));
        Duration ipWait = timeUntilNextAttempt(failuresPerIp.bucketFor(clientIp));

        Duration wait = accountWait.compareTo(ipWait) >= 0 ? accountWait : ipWait;
        if (!wait.isZero()) {
            throw new TooManyLoginAttemptsException(wait);
        }
    }

    public void recordFailure(String clientIp, String username) {
        failuresPerAccount.bucketFor(accountKey(clientIp, username)).tryConsume(1);
        failuresPerIp.bucketFor(clientIp).tryConsume(1);
    }

    /** Reinicia solo el contador de la cuenta: el de la IP sigue frenando a quien prueba muchas cuentas. */
    public void recordSuccess(String clientIp, String username) {
        failuresPerAccount.reset(accountKey(clientIp, username));
    }

    private static Duration timeUntilNextAttempt(Bucket bucket) {
        if (bucket.getAvailableTokens() > 0) {
            return Duration.ZERO;
        }
        return Duration.ofNanos(bucket.estimateAbilityToConsume(1).getNanosToWaitForRefill());
    }

    /** Sin distinguir mayúsculas ni espacios, para que " KAREN " no esquive el límite de "karen". */
    private static String accountKey(String clientIp, String username) {
        String normalized = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        return clientIp + "|" + normalized;
    }
}
