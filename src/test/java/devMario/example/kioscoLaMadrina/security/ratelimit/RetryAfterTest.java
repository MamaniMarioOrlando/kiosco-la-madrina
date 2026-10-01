package devMario.example.kioscoLaMadrina.security.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/** Retry-After se expresa en segundos enteros, redondeando hacia arriba y nunca menos de 1. */
class RetryAfterTest {

    @Test
    void exactSecondsAreNotIncremented() {
        assertThat(RetryAfter.seconds(Duration.ofSeconds(30))).isEqualTo(30);
    }

    @Test
    void fractionsRoundUp() {
        assertThat(RetryAfter.seconds(Duration.ofMillis(29_001))).isEqualTo(30);
    }

    @Test
    void neverLessThanOneSecond() {
        assertThat(RetryAfter.seconds(Duration.ZERO)).isEqualTo(1);
        assertThat(RetryAfter.seconds(Duration.ofMillis(1))).isEqualTo(1);
    }
}
