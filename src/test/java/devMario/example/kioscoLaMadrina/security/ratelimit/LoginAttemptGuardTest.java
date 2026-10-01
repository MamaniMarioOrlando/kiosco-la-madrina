package devMario.example.kioscoLaMadrina.security.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Reglas del bloqueo por intentos fallidos de login (decisión de negocio: 5 fallos cada 15 minutos por cuenta).
 * Se cuenta por IP + usuario, para que un empleado que se equivoca no bloquee al resto del kiosco
 * (todos comparten la IP del local) y un atacante no pueda bloquear una cuenta desde otra red.
 */
class LoginAttemptGuardTest {

    private static final String KIOSCO_IP = "200.1.1.1";
    private static final String ATTACKER_IP = "66.6.6.6";

    private final LoginAttemptGuard guard = new LoginAttemptGuard(
            RateLimitConfig.loginFailuresPerAccountRegistry(), RateLimitConfig.loginFailuresPerIpRegistry());

    @Test
    void allowsUpToFiveFailedAttempts() {
        failTimes(4, KIOSCO_IP, "karen");

        assertThatCode(() -> guard.checkNotBlocked(KIOSCO_IP, "karen")).doesNotThrowAnyException();
    }

    @Test
    void blocksTheAccountAfterFiveFailures_ForAtMostFifteenMinutes() {
        failTimes(5, KIOSCO_IP, "karen");

        assertThatThrownBy(() -> guard.checkNotBlocked(KIOSCO_IP, "karen"))
                .isInstanceOfSatisfying(TooManyLoginAttemptsException.class, e ->
                        assertThat(e.getRetryAfter()).isPositive().isLessThanOrEqualTo(Duration.ofMinutes(15)));
    }

    @Test
    void oneEmployeesMistakesDoNotBlockOtherEmployeesOfTheSameKiosco() {
        failTimes(5, KIOSCO_IP, "karen");

        assertThatCode(() -> guard.checkNotBlocked(KIOSCO_IP, "mario")).doesNotThrowAnyException();
    }

    @Test
    void anAttackerCannotLockAnAccountOutFromAnotherNetwork() {
        failTimes(5, ATTACKER_IP, "karen");

        assertThatCode(() -> guard.checkNotBlocked(KIOSCO_IP, "karen")).doesNotThrowAnyException();
    }

    @Test
    void usernameIsCaseInsensitive_SoTheLimitCannotBeBypassedByChangingCase() {
        failTimes(5, KIOSCO_IP, "karen");

        assertThatThrownBy(() -> guard.checkNotBlocked(KIOSCO_IP, " KAREN "))
                .isInstanceOf(TooManyLoginAttemptsException.class);
    }

    @Test
    void successfulLoginResetsTheAccountCounter() {
        failTimes(4, KIOSCO_IP, "karen");
        guard.recordSuccess(KIOSCO_IP, "karen");
        failTimes(4, KIOSCO_IP, "karen");

        assertThatCode(() -> guard.checkNotBlocked(KIOSCO_IP, "karen")).doesNotThrowAnyException();
    }

    @Test
    void blocksAnIpThatSpraysPasswordsAcrossManyAccounts() {
        for (int user = 0; user < 20; user++) {
            guard.recordFailure(ATTACKER_IP, "user" + user);
        }

        // Una cuenta que nunca falló, pero desde la misma IP: el límite por IP corta el ataque.
        assertThatThrownBy(() -> guard.checkNotBlocked(ATTACKER_IP, "nueva"))
                .isInstanceOf(TooManyLoginAttemptsException.class);
    }

    private void failTimes(int times, String ip, String username) {
        for (int i = 0; i < times; i++) {
            guard.recordFailure(ip, username);
        }
    }
}
