package com.example.invoicecsv.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents one processed CSV row with the original source values and the calculated values kept separate.
 *
 * <p>The original CSV values are preserved exactly as provided by the caller, while the calculated values
 * are stored in dedicated fields so they can be inspected independently when needed.</p>
 */
public final class InvoiceItem {

    private final String stt;
    private final String item;
    private final BigDecimal quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal vatRate;
    private final BigDecimal originalAmount;
    private final BigDecimal originalVatAmount;
    private final BigDecimal calculatedAmount;
    private final BigDecimal calculatedVatAmount;

    /**
     * Creates a new invoice item.
     *
     * @param stt the source row number value from the CSV row
     * @param item the item description
     * @param quantity the quantity value from the CSV row
     * @param unitPrice the unit price from the CSV row
     * @param vatRate the VAT rate from the CSV row
     * @param originalAmount the original amount value from the CSV row
     * @param originalVatAmount the original VAT amount value from the CSV row
     * @param calculatedAmount the calculated amount for this row
     * @param calculatedVatAmount the calculated VAT amount for this row
     */
    /**
     * Creates a new invoice item.
     *
     * <p>Negative values are not rejected by this library because the requirements do not define a negative-value
     * business rule. If a consuming application needs to prohibit them, that should be enforced explicitly in the
     * application layer.</p>
     *
     * @param stt the source row number value from the CSV row
     * @param item the item description
     * @param quantity the quantity value from the CSV row
     * @param unitPrice the unit price from the CSV row
     * @param vatRate the VAT rate from the CSV row
     * @param originalAmount the original amount value from the CSV row
     * @param originalVatAmount the original VAT amount value from the CSV row
     * @param calculatedAmount the calculated amount for this row
     * @param calculatedVatAmount the calculated VAT amount for this row
     */
    public InvoiceItem(
            String stt,
            String item,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal vatRate,
            BigDecimal originalAmount,
            BigDecimal originalVatAmount,
            BigDecimal calculatedAmount,
            BigDecimal calculatedVatAmount) {
        this.stt = Objects.requireNonNull(stt, "stt must not be null");
        this.item = Objects.requireNonNull(item, "item must not be null");
        this.quantity = requireNotNull(quantity, "quantity");
        this.unitPrice = requireNotNull(unitPrice, "unitPrice");
        this.vatRate = requireNotNull(vatRate, "vatRate");
        this.originalAmount = requireNotNull(originalAmount, "originalAmount");
        this.originalVatAmount = requireNotNull(originalVatAmount, "originalVatAmount");
        this.calculatedAmount = requireNotNull(calculatedAmount, "calculatedAmount");
        this.calculatedVatAmount = requireNotNull(calculatedVatAmount, "calculatedVatAmount");
    }

    /**
     * Returns the source row index or sequence value.
     *
     * @return the STT value
     */
    public String getStt() {
        return stt;
    }

    /**
     * Returns the item description.
     *
     * @return the item name
     */
    public String getItem() {
        return item;
    }

    /**
     * Returns the quantity value as provided in the source CSV.
     *
     * @return the quantity
     */
    public BigDecimal getQuantity() {
        return quantity;
    }

    /**
     * Returns the unit price value from the CSV.
     *
     * @return the unit price
     */
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    /**
     * Returns the VAT rate from the CSV.
     *
     * @return the VAT rate
     */
    public BigDecimal getVatRate() {
        return vatRate;
    }

    /**
     * Returns the original amount value from the CSV row.
     *
     * @return the original amount
     */
    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    /**
     * Returns the original VAT amount value from the CSV row.
     *
     * @return the original VAT amount
     */
    public BigDecimal getOriginalVatAmount() {
        return originalVatAmount;
    }

    /**
     * Returns the calculated amount for this row.
     *
     * @return the calculated amount
     */
    public BigDecimal getCalculatedAmount() {
        return calculatedAmount;
    }

    /**
     * Returns the calculated VAT amount for this row.
     *
     * @return the calculated VAT amount
     */
    public BigDecimal getCalculatedVatAmount() {
        return calculatedVatAmount;
    }

    /**
     * Creates a new item instance with updated calculated values while preserving the original row data.
     *
     * @param calculatedAmount the new calculated amount value
     * @param calculatedVatAmount the new calculated VAT amount value
     * @return a new invoice item instance with the supplied calculated values
     */
    public InvoiceItem withCalculatedValues(BigDecimal calculatedAmount, BigDecimal calculatedVatAmount) {
        return new InvoiceItem(
                stt,
                item,
                quantity,
                unitPrice,
                vatRate,
                originalAmount,
                originalVatAmount,
                calculatedAmount,
                calculatedVatAmount);
    }

    private static BigDecimal requireNotNull(BigDecimal value, String fieldName) {
        return Objects.requireNonNull(value, fieldName + " must not be null");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof InvoiceItem other)) {
            return false;
        }
        return Objects.equals(stt, other.stt)
                && Objects.equals(item, other.item)
                && Objects.equals(quantity, other.quantity)
                && Objects.equals(unitPrice, other.unitPrice)
                && Objects.equals(vatRate, other.vatRate)
                && Objects.equals(originalAmount, other.originalAmount)
                && Objects.equals(originalVatAmount, other.originalVatAmount)
                && Objects.equals(calculatedAmount, other.calculatedAmount)
                && Objects.equals(calculatedVatAmount, other.calculatedVatAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stt, item, quantity, unitPrice, vatRate,
                originalAmount, originalVatAmount, calculatedAmount, calculatedVatAmount);
    }

    @Override
    public String toString() {
        return "InvoiceItem["
                + "stt=" + stt
                + ", item=" + item
                + ", quantity=" + quantity
                + ", unitPrice=" + unitPrice
                + ", vatRate=" + vatRate
                + ", originalAmount=" + originalAmount
                + ", originalVatAmount=" + originalVatAmount
                + ", calculatedAmount=" + calculatedAmount
                + ", calculatedVatAmount=" + calculatedVatAmount
                + ']';
    }
}
