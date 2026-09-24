package com.example.invoicecsv.exception;

/**
 * Base exception for invalid CSV input in the library.
 */
public class InvoiceCsvException extends RuntimeException {

    /**
     * Creates a new CSV exception with a message.
     *
     * @param message the reason the CSV input is invalid
     */
    public InvoiceCsvException(String message) {
        super(message);
    }

    /**
     * Creates a new CSV exception with a message and cause.
     *
     * @param message the reason the CSV input is invalid
     * @param cause the underlying error that caused the failure
     */
    public InvoiceCsvException(String message, Throwable cause) {
        super(message, cause);
    }
}
