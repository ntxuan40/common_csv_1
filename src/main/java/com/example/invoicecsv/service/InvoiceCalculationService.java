package com.example.invoicecsv.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

import com.example.invoicecsv.model.InvoiceCsvRow;
import com.example.invoicecsv.model.InvoiceItem;

/**
 * Calculates derived values for invoice rows without modifying the original CSV values.
 *
 * <p>The service keeps the raw values from the source CSV and stores calculated values in separate fields.
 * Money is represented using {@link BigDecimal}, which avoids floating-point precision issues.</p>
 */
public class InvoiceCalculationService {

    private final MathContext mathContext;

    /**
     * Creates a calculation service with the default arithmetic precision.
     */
    public InvoiceCalculationService() {
        this(MathContext.DECIMAL64);
    }

    /**
     * Creates a calculation service with a custom {@link MathContext}.
     *
     * @param mathContext the precision and rounding mode used for calculations
     */
    public InvoiceCalculationService(MathContext mathContext) {
        this.mathContext = Objects.requireNonNull(mathContext, "mathContext must not be null");
    }

    /**
     * Returns the configured arithmetic context used for all calculations.
     *
     * @return the arithmetic context
     */
    public MathContext getMathContext() {
        return mathContext;
    }

    /**
     * Calculates the derived amount and VAT amount for an input row.
     *
     * <p>No rounding rule is imposed by the business requirements, so this service preserves the exact decimal
     * arithmetic implied by the input values. If a project-specific rounding strategy becomes necessary later,
     * callers can provide a custom {@link MathContext} when constructing this service.</p>
     *
     * <p>VAT values are accepted in either percent form or decimal form. For example, 10 and 0.10 both represent
     * 10% for calculation purposes.</p>
     *
     * @param row the parsed CSV row containing original source values
     * @return a processed item with calculated values stored separately from the original values
     */
    public InvoiceItem calculate(InvoiceCsvRow row) {
        Objects.requireNonNull(row, "row must not be null");

        BigDecimal quantity = row.getQuantity();
        BigDecimal unitPrice = row.getUnitPrice();
        BigDecimal vatRate = normalizeVatRate(row.getVatRate());

        BigDecimal calculatedAmount = quantity.multiply(unitPrice, mathContext);
        BigDecimal calculatedVatAmount = calculatedAmount.multiply(vatRate, mathContext);

        return new InvoiceItem(
                row.getStt(),
                row.getItem(),
                quantity,
                unitPrice,
                row.getVatRate(),
                calculatedAmount,
                calculatedVatAmount);
    }

    private BigDecimal normalizeVatRate(BigDecimal vatRate) {
        if (vatRate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (vatRate.compareTo(BigDecimal.ONE) >= 0) {
            return vatRate.divide(BigDecimal.valueOf(100), mathContext);
        }
        return vatRate;
    }
}
