package com.example.invoicecsv.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.invoicecsv.model.InvoiceCsvResult;
import com.example.invoicecsv.model.InvoiceItem;
import com.example.invoicecsv.model.InvoiceSummary;

class InvoiceSummaryServiceTest {

    private final InvoiceSummaryService service = new InvoiceSummaryService();

    @Test
        void givenOneItem_whenSummarized_thenAllTotalsAreExact() {
        InvoiceItem item = new InvoiceItem(
                "1",
                "Laptop",
                new BigDecimal("2"),
                new BigDecimal("1500.00"),
                new BigDecimal("0.10"),
                new BigDecimal("3000.00"),
                new BigDecimal("300.00"),
                new BigDecimal("3000.00"),
                new BigDecimal("300.00"));

        InvoiceCsvResult result = service.summarize(List.of(item));

        assertMoneyEquals(new BigDecimal("3000.00"), result.getSummary().getSumAmount());
        assertMoneyEquals(new BigDecimal("300.00"), result.getSummary().getSumVatAmount());
        assertMoneyEquals(new BigDecimal("3300.00"), result.getSummary().getTotal());
    }

    @Test
        void givenMultipleItems_whenSummarized_thenAmountsAndVatAreAggregated() {
        List<InvoiceItem> items = List.of(
                new InvoiceItem("1", "A", new BigDecimal("1"), new BigDecimal("100.00"), new BigDecimal("0.10"),
                        new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("100.00"), new BigDecimal("10.00")),
                new InvoiceItem("2", "B", new BigDecimal("2"), new BigDecimal("50.00"), new BigDecimal("0.20"),
                        new BigDecimal("100.00"), new BigDecimal("20.00"), new BigDecimal("100.00"), new BigDecimal("20.00"))
        );

        InvoiceCsvResult result = service.summarize(items);

        assertMoneyEquals(new BigDecimal("200.00"), result.getSummary().getSumAmount());
        assertMoneyEquals(new BigDecimal("30.00"), result.getSummary().getSumVatAmount());
        assertMoneyEquals(new BigDecimal("230.00"), result.getSummary().getTotal());
    }

    @Test
        void givenZeroValueItems_whenSummarized_thenAllTotalsAreZero() {
        List<InvoiceItem> items = List.of(
                new InvoiceItem("1", "Zero", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO),
                new InvoiceItem("2", "Zero2", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO)
        );

        InvoiceCsvResult result = service.summarize(items);

        assertMoneyEquals(BigDecimal.ZERO, result.getSummary().getSumAmount());
        assertMoneyEquals(BigDecimal.ZERO, result.getSummary().getSumVatAmount());
        assertMoneyEquals(BigDecimal.ZERO, result.getSummary().getTotal());
    }

    @Test
        void givenEmptyInput_whenSummarized_thenEmptyResultHasZeroSummary() {
        InvoiceCsvResult result = service.summarize(List.of());

        assertEquals(0, result.getItems().size());
        assertMoneyEquals(BigDecimal.ZERO, result.getSummary().getSumAmount());
        assertMoneyEquals(BigDecimal.ZERO, result.getSummary().getSumVatAmount());
        assertMoneyEquals(BigDecimal.ZERO, result.getSummary().getTotal());
    }

    @Test
        void givenLargeTotals_whenSummarized_thenTotalsRemainExact() {
        List<InvoiceItem> items = List.of(
                new InvoiceItem("1", "Big1", new BigDecimal("100"), new BigDecimal("1234567.89"), new BigDecimal("0.10"),
                        new BigDecimal("123456789.00"), new BigDecimal("12345678.90"), new BigDecimal("123456789.00"),
                        new BigDecimal("12345678.90")),
                new InvoiceItem("2", "Big2", new BigDecimal("25"), new BigDecimal("1000000.00"), new BigDecimal("0.20"),
                        new BigDecimal("25000000.00"), new BigDecimal("5000000.00"), new BigDecimal("25000000.00"),
                        new BigDecimal("5000000.00"))
        );

        InvoiceCsvResult result = service.summarize(items);

        assertMoneyEquals(new BigDecimal("148456789.00"), result.getSummary().getSumAmount());
        assertMoneyEquals(new BigDecimal("17345678.90"), result.getSummary().getSumVatAmount());
        assertMoneyEquals(new BigDecimal("165802467.90"), result.getSummary().getTotal());
    }

    @Test
        void givenDifferentVatRates_whenSummarized_thenEachItemVatIsIncludedExactly() {
        List<InvoiceItem> items = List.of(
                new InvoiceItem("1", "A", new BigDecimal("1"), new BigDecimal("100.00"), new BigDecimal("0.10"),
                        new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("100.00"), new BigDecimal("10.00")),
                new InvoiceItem("2", "B", new BigDecimal("1"), new BigDecimal("200.00"), new BigDecimal("0.15"),
                        new BigDecimal("200.00"), new BigDecimal("30.00"), new BigDecimal("200.00"), new BigDecimal("30.00"))
        );

        InvoiceCsvResult result = service.summarize(items);

        assertMoneyEquals(new BigDecimal("300.00"), result.getSummary().getSumAmount());
        assertMoneyEquals(new BigDecimal("40.00"), result.getSummary().getSumVatAmount());
        assertMoneyEquals(new BigDecimal("340.00"), result.getSummary().getTotal());
    }

    private void assertMoneyEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, actual.compareTo(expected),
                () -> "Expected value " + expected.toPlainString() + " but was " + actual.toPlainString());
    }
}
