package com.example.invoicecsv;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Objects;

import com.example.invoicecsv.model.InvoiceCsvResult;
import com.example.invoicecsv.model.InvoiceCsvRow;
import com.example.invoicecsv.model.InvoiceItem;
import com.example.invoicecsv.parser.CsvParseOptions;
import com.example.invoicecsv.parser.CsvRowParser;
import com.example.invoicecsv.service.InvoiceCalculationService;
import com.example.invoicecsv.service.InvoiceSummaryService;

/**
 * Public entry point for the CSV invoice processing library.
 *
 * <p>This class hides the internal parsing, calculation, and summary implementation details behind a simple API
 * intended for use by other Java applications.</p>
 */
public final class InvoiceCsvLibrary {

    private final CsvRowParser parser;
    private final InvoiceCalculationService calculationService;
    private final InvoiceSummaryService summaryService;

    /**
     * Creates the default library instance with the built-in parser and processing services.
     */
    public InvoiceCsvLibrary() {
        this(new CsvRowParser(), new InvoiceCalculationService(), new InvoiceSummaryService());
    }

    /**
     * Creates a library instance with custom collaborators.
     *
     * @param parser the CSV parser used for input parsing
     * @param calculationService the service used to calculate row values
     * @param summaryService the service used to aggregate results
     */
    public InvoiceCsvLibrary(
            CsvRowParser parser,
            InvoiceCalculationService calculationService,
            InvoiceSummaryService summaryService) {
        this.parser = Objects.requireNonNull(parser, "parser must not be null");
        this.calculationService = Objects.requireNonNull(calculationService, "calculationService must not be null");
        this.summaryService = Objects.requireNonNull(summaryService, "summaryService must not be null");
    }

    /**
     * Processes a CSV string and returns the final result containing processed items and summary values.
     *
     * @param csvInput the raw CSV input text
     * @param hasHeader true when the CSV contains a header row, false when it is positional data without a header
     * @return the processed result model
     */
    public InvoiceCsvResult process(String csvInput, boolean hasHeader) {
        Objects.requireNonNull(csvInput, "csvInput must not be null");
        return process(new StringReader(csvInput), new CsvParseOptions(hasHeader, ','));
    }

    /**
     * Processes CSV content read from a reader and returns the final result containing processed items and summary values.
     *
     * @param reader the source CSV reader
     * @param hasHeader true when the CSV contains a header row, false when it is positional data without a header
     * @return the processed result model
     */
    public InvoiceCsvResult process(Reader reader, boolean hasHeader) {
        Objects.requireNonNull(reader, "reader must not be null");
        return process(reader, new CsvParseOptions(hasHeader, ','));
    }

    /**
     * Processes CSV content using explicit parsing options.
     *
     * @param csvInput the raw CSV input text
     * @param options the parsing settings, including header detection and delimiter
     * @return the processed result model
     */
    public InvoiceCsvResult process(String csvInput, CsvParseOptions options) {
        Objects.requireNonNull(csvInput, "csvInput must not be null");
        return process(new StringReader(csvInput), options);
    }

    /**
     * Processes CSV content using explicit parsing options.
     *
     * @param reader the source CSV reader
     * @param options the parsing settings, including header detection and delimiter
     * @return the processed result model
     */
    public InvoiceCsvResult process(Reader reader, CsvParseOptions options) {
        Objects.requireNonNull(reader, "reader must not be null");
        Objects.requireNonNull(options, "options must not be null");

        List<InvoiceCsvRow> parsedRows = parser.parse(reader, options);
        List<InvoiceItem> calculatedItems = parsedRows.stream()
                .map(calculationService::calculate)
                .toList();

        return summaryService.summarize(calculatedItems);
    }
}
