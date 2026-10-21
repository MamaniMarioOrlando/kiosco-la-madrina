package devMario.example.kioscoLaMadrina.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** La venta mantiene sus propios ítems y su total: test unitario puro, sin Spring ni base de datos. */
class SaleTest {

    @Test
    void newSaleHasNoItemsAndZeroTotal() {
        Sale sale = new Sale();

        assertThat(sale.getDetails()).isEmpty();
        assertThat(sale.getTotalAmount()).isEqualByComparingTo("0");
    }

    @Test
    void addItem_RecordsPriceAtSaleTimeAndSubtotal() {
        Sale sale = new Sale();
        Product cola = product("Coca-Cola", "1500");

        sale.addItem(cola, 3);

        SaleDetail detail = sale.getDetails().get(0);
        assertThat(detail.getSale()).isSameAs(sale);
        assertThat(detail.getProduct()).isSameAs(cola);
        assertThat(detail.getQuantity()).isEqualTo(3);
        assertThat(detail.getUnitPrice()).isEqualByComparingTo("1500");
        assertThat(detail.getSubtotal()).isEqualByComparingTo("4500");
    }

    @Test
    void totalIsTheSumOfAllSubtotals() {
        Sale sale = new Sale();

        sale.addItem(product("Coca-Cola", "1500"), 2);   // 3000
        sale.addItem(product("Doritos", "1000.50"), 5);  // 5002.50

        assertThat(sale.getDetails()).hasSize(2);
        assertThat(sale.getTotalAmount()).isEqualByComparingTo("8002.50");
    }

    @Test
    void laterPriceChangesDoNotAlterARecordedItem() {
        Sale sale = new Sale();
        Product cola = product("Coca-Cola", "1500");
        sale.addItem(cola, 1);

        cola.setPrice(new BigDecimal("9999"));

        assertThat(sale.getDetails().get(0).getUnitPrice()).isEqualByComparingTo("1500");
        assertThat(sale.getTotalAmount()).isEqualByComparingTo("1500");
    }

    private static Product product(String name, String price) {
        return Product.builder().name(name).price(new BigDecimal(price)).stockQuantity(10).build();
    }
}
