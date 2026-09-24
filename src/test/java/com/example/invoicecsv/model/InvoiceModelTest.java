package com.example.invoicecsv.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
