package com.example.invoicecsv.exception;

/**
 * Thrown when a required CSV column is missing from the header or data definition.
 */
public class MissingColumnException extends CsvValidationException {

    /**
     * Creates a missing-column exception.
     *
     * @param message the specific missing column error
     */
    public MissingColumnException(String message) {
        super(message);
    }
}
