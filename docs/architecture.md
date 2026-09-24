# Architecture

## Scope

`common-csv` is a Java 22 library packaged as a Maven JAR. It has no UI, REST, or database layer. Processing is intentionally split into parsing, calculation, and summary components.

## Processing flow

```text
CSV String or Reader
        |
        v
InvoiceCsvLibrary
        |
        v
CsvRowParser -> List<InvoiceCsvRow>
        |
        v
InvoiceCalculationService -> List<InvoiceItem>
        |
        v
InvoiceSummaryService -> InvoiceCsvResult
```

## Public entry point

`com.example.invoicecsv.InvoiceCsvLibrary` is the consumer-facing facade. It provides `process` overloads for:

- `String` plus `boolean hasHeader`
- `Reader` plus `boolean hasHeader`
- `String` plus `CsvParseOptions`
- `Reader` plus `CsvParseOptions`

The default constructor wires the built-in parser, calculation service, and summary service. A second constructor accepts those collaborators, which allows a consuming application to provide a calculation service with a custom `MathContext`.

## Parser

`CsvRowParser` converts CSV records into `InvoiceCsvRow` objects. It owns:

- Header or positional parsing
- Required-column lookup
- CSV quoting and delimiter handling
- Row-length validation
- Required-value validation
- `BigDecimal` parsing

The parser does not calculate amounts or summaries. It ignores blank lines and does not support multiline quoted fields.

Parsing consumes the supplied `Reader` but does not close it. The caller owns the reader and is responsible for closing it.

`CsvParseOptions` contains the explicit `hasHeader` flag and the delimiter. The public convenience methods use a comma delimiter.

## Calculation component

`InvoiceCalculationService` converts each `InvoiceCsvRow` into an immutable `InvoiceItem`. It preserves the source values and stores calculated amount and VAT in separate fields.

The default service uses `MathContext.DECIMAL64`. A custom `MathContext` can be supplied through its constructor.

## Summary component

`InvoiceSummaryService` accepts calculated items and returns an `InvoiceCsvResult` containing:

- The processed item list
- An `InvoiceSummary` with amount, VAT, and total aggregates

The result item list is copied and exposed as immutable.

## Domain models

- `InvoiceCsvRow`: parsed source row before calculation.
- `InvoiceItem`: source row values plus calculated amount and calculated VAT.
- `InvoiceSummary`: aggregate calculated amount, calculated VAT, and total.
- `InvoiceCsvResult`: top-level items plus summary result.

The models are immutable and use `BigDecimal` for numeric values.

## Exception handling

`InvoiceCsvException` is the base unchecked exception for CSV failures.

- `CsvValidationException`: invalid values, blank required values, null parser input, or header-only input.
- `MissingColumnException`: a validation exception for a missing required header column.
- `CsvFormatException`: malformed quoted CSV, incorrect column count, or reader failures.

The public facade rejects null arguments with `NullPointerException`; parser methods report null input with `CsvValidationException`.
