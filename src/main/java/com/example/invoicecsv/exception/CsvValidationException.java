package com.example.invoicecsv.exception;

/**
 * Thrown when CSV data values fail validation.
 */
public class CsvValidationException extends InvoiceCsvException {

    /**
     * Creates a validation exception for the given field.
     *
     * @param message the validation error message
     */
    public CsvValidationException(String message) {
        super(message);
    }

    /**
     * Creates a validation exception with a cause.
     *
     * @param message the validation error message
     * @param cause the underlying exception that triggered the failure
     */
    public CsvValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
