package com.example.invoicecsv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.invoicecsv.model.InvoiceCsvResult;
import com.example.invoicecsv.model.InvoiceItem;
import com.example.invoicecsv.exception.CsvFormatException;
import com.example.invoicecsv.exception.CsvValidationException;

class InvoiceCsvLibraryIntegrationTest {

    private final InvoiceCsvLibrary library = new InvoiceCsvLibrary();

    @Test
    void givenHeaderCsv_whenProcessed_thenCompleteResultIsCalculatedExactly() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT,Thành tiền,VAT Amt",
                "1,Laptop,2,1500,10,3000,300",
                "2,Mouse,5,200,10,1000,100");

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
                "1,Laptop,2,1500,10,3000,300",
                "2,Mouse,5,200,10,1000,100");

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
    void givenHeaderCsv_whenProcessed_thenOriginalValuesRemainSeparateFromCalculatedValues() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT,Thành tiền,VAT Amt",
                "1,Laptop,2,1500,10,3000,300");

        InvoiceCsvResult result = library.process(csv, true);
        InvoiceItem item = result.getItems().get(0);

        assertMoneyEquals(new java.math.BigDecimal("3000.00"), item.getOriginalAmount());
        assertMoneyEquals(new java.math.BigDecimal("300.00"), item.getOriginalVatAmount());
        assertMoneyEquals(new java.math.BigDecimal("3000.00"), item.getCalculatedAmount());
        assertMoneyEquals(new java.math.BigDecimal("300.00"), item.getCalculatedVatAmount());
    }

    @Test
    void givenInvalidNumericInput_whenProcessed_thenValidationExceptionIsPropagated() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT,Thành tiền,VAT Amt",
                "1,Laptop,invalid,1500,10,3000,300");

        assertThrows(CsvValidationException.class, () -> library.process(csv, true));
    }

    @Test
    void givenMalformedCsv_whenProcessed_thenFormatExceptionIsPropagated() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT,Thành tiền,VAT Amt",
                "1,\"Laptop,2,1500,10,3000,300");

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
