package com.example.invoicecsv.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents the aggregated totals for a processed CSV input.
 *
 * <p>The summary holds the sum of computed amounts, the sum of computed VAT values, and the total.
 * The values are stored as immutable {@link BigDecimal} values to avoid floating-point precision issues.</p>
 */
public final class InvoiceSummary {

    private final BigDecimal sumAmount;
    private final BigDecimal sumVatAmount;
    private final BigDecimal total;

    /**
     * Creates a summary with the provided aggregate values.
     *
     * @param sumAmount the total calculated amount across all rows
     * @param sumVatAmount the total calculated VAT amount across all rows
     * @param total the final total amount including VAT
     */
    /**
     * Creates a summary with the provided aggregate values.
     *
     * <p>The library does not impose a negative-value business rule here because the requirements do not specify one.
     * If a consuming application needs to prohibit negative totals, that should be enforced explicitly in the domain
     * layer outside this library.</p>
     *
     * @param sumAmount the total calculated amount across all rows
     * @param sumVatAmount the total calculated VAT amount across all rows
     * @param total the final total amount including VAT
     */
    public InvoiceSummary(BigDecimal sumAmount, BigDecimal sumVatAmount, BigDecimal total) {
        this.sumAmount = requireNotNull(sumAmount, "sumAmount");
        this.sumVatAmount = requireNotNull(sumVatAmount, "sumVatAmount");
        this.total = requireNotNull(total, "total");
    }

    /**
     * Creates a summary using the supplied values.
     *
     * @param sumAmount the total calculated amount
     * @param sumVatAmount the total calculated VAT amount
     * @param total the combined total
     * @return a new summary instance
     */
    public static InvoiceSummary of(BigDecimal sumAmount, BigDecimal sumVatAmount, BigDecimal total) {
        return new InvoiceSummary(sumAmount, sumVatAmount, total);
    }

    /**
     * Returns the sum of all calculated amounts.
     *
     * @return the sum amount
     */
    public BigDecimal getSumAmount() {
        return sumAmount;
    }

    /**
     * Returns the sum of all calculated VAT amounts.
     *
     * @return the sum VAT amount
     */
    public BigDecimal getSumVatAmount() {
        return sumVatAmount;
    }

    /**
     * Returns the total amount including VAT.
     *
     * @return the final total
     */
    public BigDecimal getTotal() {
        return total;
    }

    private static BigDecimal requireNotNull(BigDecimal value, String fieldName) {
        return Objects.requireNonNull(value, fieldName + " must not be null");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof InvoiceSummary other)) {
            return false;
        }
        return Objects.equals(sumAmount, other.sumAmount)
                && Objects.equals(sumVatAmount, other.sumVatAmount)
                && Objects.equals(total, other.total);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sumAmount, sumVatAmount, total);
    }

    @Override
    public String toString() {
        return "InvoiceSummary[sumAmount=" + sumAmount
                + ", sumVatAmount=" + sumVatAmount
                + ", total=" + total
                + ']';
    }
}
