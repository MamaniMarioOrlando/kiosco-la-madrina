package devMario.example.kioscoLaMadrina.integration;

import devMario.example.kioscoLaMadrina.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Defensa en profundidad: la base rechaza datos imposibles aunque alguien se saltee la aplicación
 * (un script, una consulta manual en la consola de Neon o un bug futuro en el código).
 * Por eso estos tests escriben con SQL directo, sin pasar por las validaciones de Java.
 */
@ActiveProfiles("test")
class DatabaseConstraintsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void stockCannotBeNegative() {
        assertRejected(() -> insertProduct("100", -1));
    }

    @Test
    void priceMustBePositive() {
        assertRejected(() -> insertProduct("0", 1));
    }

    @Test
    void soldQuantityMustBePositive() {
        long productId = insertProduct("100", 1);
        long saleId = insertSale();

        assertRejected(() -> insertDetail(saleId, productId, 0));
    }

    @Test
    void saleDetailMustBelongToASaleAndAProduct() {
        long productId = insertProduct("100", 1);
        long saleId = insertSale();

        assertRejected(() -> insertDetail(null, productId, 1));
        assertRejected(() -> insertDetail(saleId, null, 1));
    }

    @Test
    void saleMustHaveAUser() {
        assertRejected(() -> jdbc.update("""
                INSERT INTO sales (date_time, total_amount, payment_method, user_id)
                VALUES (now(), 0, 'CASH', NULL)"""));
    }

    private long insertProduct(String price, int stock) {
        return jdbc.queryForObject("""
                        INSERT INTO products (barcode, name, price, stock_quantity)
                        VALUES (?, 'Producto', ?::numeric, ?) RETURNING id""",
                Long.class, "db-" + UUID.randomUUID(), price, stock);
    }

    private long insertSale() {
        long userId = jdbc.queryForObject("""
                        INSERT INTO users (username, password, role)
                        VALUES (?, 'x', 'EMPLOYEE') RETURNING id""",
                Long.class, "db-" + UUID.randomUUID());
        return jdbc.queryForObject("""
                        INSERT INTO sales (date_time, total_amount, payment_method, user_id)
                        VALUES (now(), 0, 'CASH', ?) RETURNING id""",
                Long.class, userId);
    }

    private void insertDetail(Long saleId, Long productId, int quantity) {
        jdbc.update("""
                        INSERT INTO sale_details (sale_id, product_id, quantity, unit_price, subtotal)
                        VALUES (?, ?, ?, 100, 100)""",
                saleId, productId, quantity);
    }

    private static void assertRejected(Runnable insert) {
        assertThatThrownBy(insert::run).isInstanceOf(DataIntegrityViolationException.class);
    }
}
