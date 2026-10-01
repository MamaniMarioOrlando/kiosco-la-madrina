package devMario.example.kioscoLaMadrina.security.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

import java.time.Duration;

/**
 * Un balde de rate limiting por clave (una IP, una cuenta…), con memoria acotada.
 *
 * Reemplaza a un Map que crecía para siempre: cada IP nueva agregaba un balde que nunca se borraba,
 * así que muchas IPs distintas podían agotar la memoria del servidor.
 *
 * - Tamaño máximo: con muchas claves, Caffeine descarta las menos usadas.
 * - Vencimiento por inactividad: igual al tiempo que tarda el balde en rellenarse por completo.
 *   Así, borrar un balde y crear uno nuevo y lleno nunca regala fichas que el original no tendría.
 *   Se calcula a partir de la política para que esa regla no dependa de quien configure la clase.
 */
public class BucketRegistry {

    private final Bandwidth limit;
    private final Cache<String, Bucket> buckets;

    public BucketRegistry(Bandwidth limit, long maxKeys) {
        this.limit = limit;
        this.buckets = Caffeine.newBuilder()
                .maximumSize(maxKeys)
                .expireAfterAccess(timeToRefillCompletely(limit))
                .build();
    }

    public Bucket bucketFor(String key) {
        return buckets.get(key, k -> Bucket.builder().addLimit(limit).build());
    }

    public void reset(String key) {
        buckets.invalidate(key);
    }

    /** Cantidad de baldes en memoria (para tests y métricas). */
    long size() {
        buckets.cleanUp();
        return buckets.estimatedSize();
    }

    private static Duration timeToRefillCompletely(Bandwidth limit) {
        long nanos = Math.ceilDiv(limit.getCapacity() * limit.getRefillPeriodNanos(), limit.getRefillTokens());
        return Duration.ofNanos(nanos);
    }
}
