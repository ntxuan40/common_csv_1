package com.example.invoicecsv.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a single CSV row before calculation logic is applied.
 *
 * <p>The object stores the parsed input values before calculation logic is applied.</p>
 */
public final class InvoiceCsvRow {

    private final String stt;
    private final String item;
    private final BigDecimal quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal vatRate;
    /**
     * Creates a parsed CSV row.
     *
     * @param stt the original STT value
     * @param item the item name or description
     * @param quantity the parsed quantity
     * @param unitPrice the parsed unit price
     * @param vatRate the parsed VAT rate
     */
    /**
     * Creates a parsed CSV row.
     *
     * <p>Note: this library intentionally does not impose a negative-number business rule beyond ensuring the
     * values are present and parseable. If a consuming application needs to forbid negative values, that should be
     * enforced in the application layer as an explicit domain policy.</p>
     *
     * @param stt the original STT value
     * @param item the item name or description
     * @param quantity the parsed quantity
     * @param unitPrice the parsed unit price
     * @param vatRate the parsed VAT rate
     */
    public InvoiceCsvRow(
            String stt,
            String item,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal vatRate) {
        this.stt = requireNonBlank(stt, "stt");
        this.item = requireNonBlank(item, "item");
        this.quantity = requireNotNull(quantity, "quantity");
        this.unitPrice = requireNotNull(unitPrice, "unitPrice");
        this.vatRate = requireNotNull(vatRate, "vatRate");
    }

    /**
     * Returns the STT field.
     *
     * @return row sequence value
     */
    public String getStt() {
        return stt;
    }

    /**
     * Returns the item description.
     *
     * @return item description
     */
    public String getItem() {
        return item;
    }

    /**
     * Returns the quantity value.
     *
     * @return quantity
     */
    public BigDecimal getQuantity() {
        return quantity;
    }

    /**
     * Returns the unit price value.
     *
     * @return unit price
     */
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    /**
     * Returns the VAT rate value.
     *
     * @return VAT rate
     */
    public BigDecimal getVatRate() {
        return vatRate;
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static BigDecimal requireNotNull(BigDecimal value, String fieldName) {
        return Objects.requireNonNull(value, fieldName + " must not be null");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof InvoiceCsvRow other)) {
            return false;
        }
        return Objects.equals(stt, other.stt)
                && Objects.equals(item, other.item)
                && Objects.equals(quantity, other.quantity)
                && Objects.equals(unitPrice, other.unitPrice)
                && Objects.equals(vatRate, other.vatRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stt, item, quantity, unitPrice, vatRate);
    }

    @Override
    public String toString() {
        return "InvoiceCsvRow[stt=" + stt
                + ", item=" + item
                + ", quantity=" + quantity
                + ", unitPrice=" + unitPrice
                + ", vatRate=" + vatRate
                + ']';
    }
}
