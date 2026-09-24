package com.example.invoicecsv.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the result of processing a CSV input.
 *
 * <p>The result contains the processed row information and the corresponding summary for the whole dataset.</p>
 */
public final class InvoiceCsvResult {

    private final List<InvoiceItem> items;
    private final InvoiceSummary summary;

    /**
     * Creates a result model for the given processed rows and summary.
     *
     * @param items the list of processed invoice items
     * @param summary the aggregated summary values
     */
    public InvoiceCsvResult(List<InvoiceItem> items, InvoiceSummary summary) {
        this.items = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(items, "items must not be null")));
        this.summary = Objects.requireNonNull(summary, "summary must not be null");
    }

    /**
     * Returns the processed items.
     *
     * @return an immutable list of invoice items
     */
    public List<InvoiceItem> getItems() {
        return items;
    }

    /**
     * Returns the summary for the processed dataset.
     *
     * @return the invoice summary
     */
    public InvoiceSummary getSummary() {
        return summary;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof InvoiceCsvResult other)) {
            return false;
        }
        return Objects.equals(items, other.items) && Objects.equals(summary, other.summary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, summary);
    }

    @Override
    public String toString() {
        return "InvoiceCsvResult[items=" + items + ", summary=" + summary + ']';
    }
}
