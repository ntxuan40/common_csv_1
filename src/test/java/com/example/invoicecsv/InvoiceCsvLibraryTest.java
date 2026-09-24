package com.example.invoicecsv;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.example.invoicecsv.model.InvoiceCsvResult;

class InvoiceCsvLibraryTest {

    @Test
    void givenEmptyCsv_whenProcessed_thenEmptyResultHasZeroSummary() {
        InvoiceCsvResult result = new InvoiceCsvLibrary().process("", true);

        assertEquals(0, result.getItems().size());
        assertMoneyEquals("0", result.getSummary().getSumAmount());
        assertMoneyEquals("0", result.getSummary().getSumVatAmount());
        assertMoneyEquals("0", result.getSummary().getTotal());
    }

    private void assertMoneyEquals(String expected, BigDecimal actual) {
        assertEquals(0, actual.compareTo(new BigDecimal(expected)),
                () -> "Expected value " + expected + " but was " + actual.toPlainString());
    }
}
