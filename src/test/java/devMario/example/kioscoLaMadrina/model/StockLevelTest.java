package devMario.example.kioscoLaMadrina.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/** Regla de negocio del stock: test unitario puro, centrado en los bordes donde cambia el nivel. */
class StockLevelTest {

    @ParameterizedTest(name = "{0} unidades → {1}")
    @CsvSource({
            "0, CRITICAL",
            "2, CRITICAL",
            "3, LOW",
            "4, LOW",
            "5, OK",
            "100, OK",
    })
    void levelDependsOnTheUnitsLeft(int units, StockLevel expected) {
        assertThat(StockLevel.of(units)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0} → necesita reposición: {1}")
    @CsvSource({"CRITICAL, true", "LOW, true", "OK, false"})
    void criticalIsAlsoLowStock(StockLevel level, boolean needsRestock) {
        assertThat(level.needsRestock()).isEqualTo(needsRestock);
    }
}
