package devMario.example.kioscoLaMadrina.security.ratelimit;

import io.github.bucket4j.Bandwidth;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class BucketRegistryTest {

    private static final Bandwidth TWO_PER_MINUTE = Bandwidth.builder()
            .capacity(2)
            .refillGreedy(2, Duration.ofMinutes(1))
            .build();

    @Test
    void eachKeyHasItsOwnBucket() {
        BucketRegistry registry = new BucketRegistry(TWO_PER_MINUTE, 1_000);

        assertThat(registry.bucketFor("1.1.1.1").tryConsume(2)).isTrue();
        assertThat(registry.bucketFor("1.1.1.1").tryConsume(1)).isFalse();

        assertThat(registry.bucketFor("2.2.2.2").tryConsume(1)).isTrue();
    }

    @Test
    void resetGivesTheKeyAFullBucketAgain() {
        BucketRegistry registry = new BucketRegistry(TWO_PER_MINUTE, 1_000);
        registry.bucketFor("1.1.1.1").tryConsume(2);

        registry.reset("1.1.1.1");

        assertThat(registry.bucketFor("1.1.1.1").tryConsume(2)).isTrue();
    }

    @Test
    void memoryIsBounded_EvenWithManyDistinctKeys() {
        BucketRegistry registry = new BucketRegistry(TWO_PER_MINUTE, 100);

        for (int i = 0; i < 10_000; i++) {
            registry.bucketFor("10.0." + (i / 256) + "." + (i % 256));
        }

        assertThat(registry.size()).isLessThanOrEqualTo(100);
    }
}
