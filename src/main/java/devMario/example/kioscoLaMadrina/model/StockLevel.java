package devMario.example.kioscoLaMadrina.model;

/**
 * Cuánto stock le queda a un producto, según las reglas del kiosco. Única fuente de los umbrales:
 * el frontend recibe el nivel ya calculado y solo decide cómo pintarlo.
 */
public enum StockLevel {
    /** Está por agotarse: alerta llamativa. */
    CRITICAL,
    /** Hay que reponerlo pronto. */
    LOW,
    OK;

    /** Con menos unidades que esto, el producto está por agotarse. */
    static final int CRITICAL_THRESHOLD = 3;
    /** Con menos unidades que esto, hay que reponerlo. */
    public static final int LOW_THRESHOLD = 5;

    public static StockLevel of(int units) {
        if (units < CRITICAL_THRESHOLD) return CRITICAL;
        if (units < LOW_THRESHOLD) return LOW;
        return OK;
    }

    /** Crítico también es bajo: los dos entran en la lista de reposición. */
    public boolean needsRestock() {
        return this != OK;
    }
}
