package com.example.invoicecsv.parser;

import java.util.Objects;

/**
 * Encapsulates parsing options for CSV input.
 */
public final class CsvParseOptions {

    private final boolean hasHeader;
    private final char delimiter;

    /**
     * Creates parsing options.
     *
     * @param hasHeader true when the CSV has a header row, false when it is positional data without a header
     * @param delimiter the column delimiter used in the CSV input
     */
    public CsvParseOptions(boolean hasHeader, char delimiter) {
        this.hasHeader = hasHeader;
        this.delimiter = delimiter;
    }

    /**
     * Returns true when the CSV includes a header row.
     *
     * @return true if a header is expected, otherwise false
     */
    public boolean hasHeader() {
        return hasHeader;
    }

    /**
     * Returns the delimiter used to separate CSV columns.
     *
     * @return the column delimiter
     */
    public char getDelimiter() {
        return delimiter;
    }

    /**
     * Creates default parse options using a comma separator and assuming the CSV has a header.
     *
     * @return default parsing configuration
     */
    public static CsvParseOptions defaults() {
        return new CsvParseOptions(true, ',');
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CsvParseOptions other)) {
            return false;
        }
        return hasHeader == other.hasHeader && delimiter == other.delimiter;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hasHeader, delimiter);
    }

    @Override
    public String toString() {
        return "CsvParseOptions[hasHeader=" + hasHeader + ", delimiter=" + delimiter + ']';
    }
}
