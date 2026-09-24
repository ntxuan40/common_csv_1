package com.example.invoicecsv.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.MathContext;

import org.junit.jupiter.api.Test;

import com.example.invoicecsv.model.InvoiceCsvRow;
import com.example.invoicecsv.model.InvoiceItem;

class InvoiceCalculationServiceTest {

    private final InvoiceCalculationService service = new InvoiceCalculationService();

    @Test
    void givenNormalValues_whenCalculated_thenAmountAndVatAreExact() {
        InvoiceItem item = service.calculate(new InvoiceCsvRow(
                "1",
                "Laptop",
                new BigDecimal("2"),
                new BigDecimal("1500.00"),
                new BigDecimal("0.10"))
        );

        assertMoneyEquals(new BigDecimal("3000.00"), item.getCalculatedAmount());
        assertMoneyEquals(new BigDecimal("300.00"), item.getCalculatedVatAmount());
    }

    @Test
    void givenZeroQuantity_whenCalculated_thenAmountAndVatAreZero() {
        InvoiceItem item = service.calculate(new InvoiceCsvRow(
                "1",
                "Mouse",
                BigDecimal.ZERO,
                new BigDecimal("25.00"),
                new BigDecimal("0.10"))
        );

        assertMoneyEquals(BigDecimal.ZERO, item.getCalculatedAmount());
        assertMoneyEquals(BigDecimal.ZERO, item.getCalculatedVatAmount());
    }

    @Test
    void givenZeroUnitPrice_whenCalculated_thenAmountAndVatAreZero() {
        InvoiceItem item = service.calculate(new InvoiceCsvRow(
                "1",
                "Cable",
                new BigDecimal("3"),
                BigDecimal.ZERO,
                new BigDecimal("0.10"))
        );

        assertMoneyEquals(BigDecimal.ZERO, item.getCalculatedAmount());
        assertMoneyEquals(BigDecimal.ZERO, item.getCalculatedVatAmount());
    }

    @Test
    void givenZeroVatRate_whenCalculated_thenAmountIsExactAndVatIsZero() {
        InvoiceItem item = service.calculate(new InvoiceCsvRow(
                "1",
                "Headphones",
                new BigDecimal("4"),
                new BigDecimal("100.00"),
                BigDecimal.ZERO)
        );

        assertMoneyEquals(new BigDecimal("400.00"), item.getCalculatedAmount());
        assertMoneyEquals(BigDecimal.ZERO, item.getCalculatedVatAmount());
    }

    @Test
    void givenEightPercentVatRate_whenCalculated_thenVatUsesTheRateExactly() {
        InvoiceItem item = service.calculate(new InvoiceCsvRow(
                "1",
                "Monitor",
                new BigDecimal("1"),
                new BigDecimal("500.00"),
                new BigDecimal("0.08"))
        );

        assertMoneyEquals(new BigDecimal("500.00"), item.getCalculatedAmount());
        assertMoneyEquals(new BigDecimal("40.00"), item.getCalculatedVatAmount());
    }

    @Test
    void givenDecimalValues_whenCalculated_thenFractionalResultsAreExact() {
        InvoiceItem item = service.calculate(new InvoiceCsvRow(
                "1",
                "Pen",
                new BigDecimal("3"),
                new BigDecimal("12.50"),
                new BigDecimal("0.10"))
        );

        assertMoneyEquals(new BigDecimal("37.50"), item.getCalculatedAmount());
        assertMoneyEquals(new BigDecimal("3.75"), item.getCalculatedVatAmount());
    }

    @Test
    void givenLargeValues_whenCalculated_thenResultsRemainExact() {
        InvoiceItem item = service.calculate(new InvoiceCsvRow(
                "1",
                "Server",
                new BigDecimal("25"),
                new BigDecimal("1234567.89"),
                new BigDecimal("0.20"))
        );

        assertMoneyEquals(new BigDecimal("30864197.25"), item.getCalculatedAmount());
        assertMoneyEquals(new BigDecimal("6172839.45"), item.getCalculatedVatAmount());
    }

    @Test
    void givenLimitedPrecision_whenCalculated_thenConfiguredRoundingIsApplied() {
        InvoiceCalculationService preciseService = new InvoiceCalculationService(new MathContext(5, java.math.RoundingMode.HALF_UP));

        InvoiceItem item = preciseService.calculate(new InvoiceCsvRow(
                "1",
                "Widget",
                new BigDecimal("1.23456"),
                new BigDecimal("7.89123"),
                new BigDecimal("0.10"))
        );

        assertMoneyEquals(new BigDecimal("9.7422"), item.getCalculatedAmount());
        assertMoneyEquals(new BigDecimal("0.97422"), item.getCalculatedVatAmount());
    }

    private void assertMoneyEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, actual.compareTo(expected),
                () -> "Expected value " + expected.toPlainString() + " but was " + actual.toPlainString());
    }
}
