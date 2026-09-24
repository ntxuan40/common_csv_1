package com.example.invoicecsv.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class InvoiceModelTest {

    @Test
    void invoiceItemPreservesOriginalAndCalculatedValues() {
        InvoiceItem item = new InvoiceItem(
                "1",
                "Laptop",
                new BigDecimal("2"),
                new BigDecimal("1500.00"),
                new BigDecimal("0.10"),
                new BigDecimal("3000.00"),
                new BigDecimal("300.00")
        );

        assertEquals("1", item.getStt());
        assertEquals("Laptop", item.getItem());
        assertEquals(new BigDecimal("3000.00"), item.getCalculatedAmount());
        assertEquals(new BigDecimal("300.00"), item.getCalculatedVatAmount());
    }

    @Test
    void invoiceSummaryAndResultContainAggregatedValues() {
        InvoiceSummary summary = new InvoiceSummary(
                new BigDecimal("3000.00"),
                new BigDecimal("300.00"),
                new BigDecimal("3300.00")
        );

        InvoiceItem item = new InvoiceItem(
                "1",
                "Laptop",
                new BigDecimal("2"),
                new BigDecimal("1500.00"),
                new BigDecimal("0.10"),
                new BigDecimal("3000.00"),
                new BigDecimal("300.00")
        );

        InvoiceCsvResult result = new InvoiceCsvResult(List.of(item), summary);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(summary, result.getSummary());
        assertEquals(new BigDecimal("3300.00"), result.getSummary().getTotal());
        assertThrows(UnsupportedOperationException.class, () -> result.getItems().add(item));
    }
    
    @Test
    void testInvoiceItemMethods() {
        // 1. Tạo các đối tượng test (Dùng constructor đầy đủ của bạn ở dòng 140)
        InvoiceItem item1 = new InvoiceItem("1", "Sản phẩm A", BigDecimal.TEN, BigDecimal.valueOf(100), BigDecimal.valueOf(0.1),BigDecimal.valueOf(0),BigDecimal.valueOf(0));
        InvoiceItem item2 = new InvoiceItem("1", "Sản phẩm A", BigDecimal.TEN, BigDecimal.valueOf(100), BigDecimal.valueOf(0.1),BigDecimal.valueOf(0),BigDecimal.valueOf(0));
        InvoiceItem item3 = new InvoiceItem("2", "Sản phẩm B", BigDecimal.ONE, BigDecimal.valueOf(200), BigDecimal.valueOf(0.1),BigDecimal.valueOf(0),BigDecimal.valueOf(0));

        // 2. Phủ code cho các hàm Getter (getStt, getItem, getQuantity, v.v...)
        assertEquals("1", item1.getStt());
        assertEquals("Sản phẩm A", item1.getItem());
        assertEquals(BigDecimal.TEN, item1.getQuantity());
        assertEquals(BigDecimal.valueOf(100), item1.getUnitPrice());
        assertEquals(BigDecimal.valueOf(0.1), item1.getVatRate());

        // 3. Phủ code cho hàm static requireNonNull (Dòng 143)
        // Gọi hàm hoặc kích hoạt nó thông qua một hành động hợp lệ, hoặc test trực tiếp nếu nó public
        // Ở đây nó là private nên nó tự động được phủ khi bạn truyền các giá trị non-null vào constructor/hàm tính toán.
        
        // 4. Phủ code cho hàm withCalculatedValues() (Dòng 132)
        InvoiceItem calculatedItem = item1.withCalculatedValues(BigDecimal.valueOf(1000), BigDecimal.valueOf(100));
        assertNotNull(calculatedItem);
        assertEquals(BigDecimal.valueOf(1000), calculatedItem.getCalculatedAmount());
        assertEquals(BigDecimal.valueOf(100), calculatedItem.getCalculatedVatAmount());

        // 5. Phủ code cho hàm equals() (Dòng 148) - Phủ hết tất cả các nhánh if/else đỏ
        assertTrue(item1.equals(item1));          // Nhánh: this == obj
        assertFalse(item1.equals(null));         // Nhánh: obj == null
        assertFalse(item1.equals("ChuoiBatKy")); // Nhánh: khac kiểu dữ liệu
        assertTrue(item1.equals(item2));          // Nhánh: các thuộc tính giống nhau
        assertFalse(item1.equals(item3));         // Nhánh: các thuộc tính khác nhau

        // 6. Phủ code cho hàm hashCode() (Dòng 165)
        assertEquals(item1.hashCode(), item2.hashCode());
        assertNotEquals(item1.hashCode(), item3.hashCode());

        // 7. Phủ code cho hàm toString() (Dòng 169)
        assertNotNull(item1.toString());
        assertTrue(item1.toString().contains("InvoiceItem"));
    }
}
