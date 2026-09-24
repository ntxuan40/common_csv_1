package com.example.invoicecsv.exception;

/**
 * Thrown when the CSV structure is malformed or cannot be parsed correctly.
 */
public class CsvFormatException extends InvoiceCsvException {

    /**
     * Creates a malformed CSV exception.
     *
     * @param message the parsing problem description
     */
    public CsvFormatException(String message) {
        super(message);
    }

    /**
     * Creates a malformed CSV exception with a cause.
     *
     * @param message the parsing problem description
     * @param cause the underlying exception that triggered the failure
     */
    public CsvFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
