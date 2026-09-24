package com.example.invoicecsv.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import com.example.invoicecsv.model.InvoiceCsvResult;
import com.example.invoicecsv.model.InvoiceItem;
import com.example.invoicecsv.model.InvoiceSummary;

/**
 * Aggregates calculated values across all processed invoice rows into a summary.
 *
 * <p>The summary is calculated from the processed item list without mutating any individual row values.</p>
 */
public class InvoiceSummaryService {

    /**
     * Creates a summary service.
     */
    public InvoiceSummaryService() {
    }

    /**
     * Builds a result object containing the processed items and their aggregate summary.
     *
     * @param items the calculated items to summarize
     * @return a result containing the list of items and the summary values
     */
    public InvoiceCsvResult summarize(List<InvoiceItem> items) {
        Objects.requireNonNull(items, "items must not be null");

        if (items.isEmpty()) {
            InvoiceSummary summary = new InvoiceSummary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            return new InvoiceCsvResult(List.of(), summary);
        }

        BigDecimal sumAmount = BigDecimal.ZERO;
        BigDecimal sumVatAmount = BigDecimal.ZERO;

        for (InvoiceItem item : items) {
            Objects.requireNonNull(item, "items must not contain null elements");
            sumAmount = sumAmount.add(item.getCalculatedAmount());
            sumVatAmount = sumVatAmount.add(item.getCalculatedVatAmount());
        }

        BigDecimal total = sumAmount.add(sumVatAmount);
        InvoiceSummary summary = new InvoiceSummary(sumAmount, sumVatAmount, total);
        return new InvoiceCsvResult(items, summary);
    }
}
