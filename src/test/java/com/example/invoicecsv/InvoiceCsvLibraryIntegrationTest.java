package com.example.invoicecsv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.invoicecsv.model.InvoiceCsvResult;
import com.example.invoicecsv.exception.CsvFormatException;
import com.example.invoicecsv.exception.CsvValidationException;

class InvoiceCsvLibraryIntegrationTest {

    private final InvoiceCsvLibrary library = new InvoiceCsvLibrary();

    @Test
    void givenHeaderCsv_whenProcessed_thenCompleteResultIsCalculatedExactly() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT",
                "1,Laptop,2,1500,10",
                "2,Mouse,5,200,10");

        InvoiceCsvResult result = library.process(csv, true);

        assertEquals(2, result.getItems().size());
        assertMoneyEquals(new java.math.BigDecimal("3000.00"), result.getItems().get(0).getCalculatedAmount());
        assertMoneyEquals(new java.math.BigDecimal("300.00"), result.getItems().get(0).getCalculatedVatAmount());
        assertMoneyEquals(new java.math.BigDecimal("1000.00"), result.getItems().get(1).getCalculatedAmount());
        assertMoneyEquals(new java.math.BigDecimal("100.00"), result.getItems().get(1).getCalculatedVatAmount());
        assertMoneyEquals(new java.math.BigDecimal("4000.00"), result.getSummary().getSumAmount());
        assertMoneyEquals(new java.math.BigDecimal("400.00"), result.getSummary().getSumVatAmount());
        assertMoneyEquals(new java.math.BigDecimal("4400.00"), result.getSummary().getTotal());
    }

    @Test
    void givenCsvWithoutHeader_whenProcessed_thenCompleteResultIsCalculatedExactly() {
        String csv = String.join(System.lineSeparator(),
                "1,Laptop,2,1500,10",
                "2,Mouse,5,200,10");

        InvoiceCsvResult result = library.process(csv, false);

        assertEquals(2, result.getItems().size());
        assertMoneyEquals(new java.math.BigDecimal("3000.00"), result.getItems().get(0).getCalculatedAmount());
        assertMoneyEquals(new java.math.BigDecimal("300.00"), result.getItems().get(0).getCalculatedVatAmount());
        assertMoneyEquals(new java.math.BigDecimal("1000.00"), result.getItems().get(1).getCalculatedAmount());
        assertMoneyEquals(new java.math.BigDecimal("100.00"), result.getItems().get(1).getCalculatedVatAmount());
        assertMoneyEquals(new java.math.BigDecimal("4000.00"), result.getSummary().getSumAmount());
        assertMoneyEquals(new java.math.BigDecimal("400.00"), result.getSummary().getSumVatAmount());
        assertMoneyEquals(new java.math.BigDecimal("4400.00"), result.getSummary().getTotal());
    }

    @Test
    void givenCsvFile_whenProcessed_thenCompleteResultIsCalculatedExactly() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/invoice-with-header.csv"), StandardCharsets.UTF_8))) {
            InvoiceCsvResult result = library.process(reader, true);

            System.out.println("CSV file processing result:");
            result.getItems().forEach(item -> System.out.println("  " + item));
            System.out.println("  Summary: " + result.getSummary());

            assertEquals(2, result.getItems().size());
            assertMoneyEquals(new java.math.BigDecimal("3000.00"), result.getItems().get(0).getCalculatedAmount());
            assertMoneyEquals(new java.math.BigDecimal("300.00"), result.getItems().get(0).getCalculatedVatAmount());
            assertMoneyEquals(new java.math.BigDecimal("4000.00"), result.getSummary().getSumAmount());
            assertMoneyEquals(new java.math.BigDecimal("400.00"), result.getSummary().getSumVatAmount());
            assertMoneyEquals(new java.math.BigDecimal("4400.00"), result.getSummary().getTotal());
        }
    }
    
    

    @Test
    void givenInvalidNumericInput_whenProcessed_thenValidationExceptionIsPropagated() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT",
                "1,Laptop,invalid,1500,10");

        assertThrows(CsvValidationException.class, () -> library.process(csv, true));
    }

    @Test
    void givenMalformedCsv_whenProcessed_thenFormatExceptionIsPropagated() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT",
                "1,\"Laptop,2,1500,10");

        assertThrows(CsvFormatException.class, () -> library.process(csv, true));
    }

    @Test
    void givenNullCsv_whenProcessed_thenNullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> library.process((String) null, true));
    }

    private void assertMoneyEquals(java.math.BigDecimal expected, java.math.BigDecimal actual) {
        assertEquals(0, actual.compareTo(expected),
                () -> "Expected value " + expected.toPlainString() + " but was " + actual.toPlainString());
    }
}
