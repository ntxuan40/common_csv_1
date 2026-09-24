# common-csv

`common-csv` is a Java library for parsing invoice CSV data, calculating row amounts and VAT, and producing aggregate totals. It is packaged as a reusable Maven JAR for use by another Java application.

The library contains no UI, REST API, or database integration.

## Features

- Parses CSV input with or without a header row.
- Requires the caller to explicitly indicate whether a header exists.
- Supports reordered required columns when a header is present.
- Supports quoted values and escaped double quotes.
- Converts numeric CSV values to `BigDecimal`.
- Calculates amount and VAT for every row.
- Preserves original CSV amounts separately from calculated values.
- Produces immutable result and item lists.
- Aggregates calculated amounts, calculated VAT, and final totals.
- Reports malformed structure and invalid values with typed runtime exceptions.

## Requirements

- Java 22
- Maven

The project is compiled with Java release 22 and packaged as a JAR.

## Input CSV Format

The logical columns are:

| Column | Meaning | Type |
| --- | --- | --- |
| `STT` | Source row number or identifier | Text |
| `Item` | Item name or description | Text |
| `Số lượng` | Quantity | Decimal number |
| `Đơn giá` | Unit price | Decimal number |
| `% VAT` | VAT rate, supplied as a percent or decimal | Decimal number |
| `Thành tiền` | Original amount from the source CSV | Decimal number |
| `VAT Amt` | Original VAT amount from the source CSV | Decimal number |

The default public API uses a comma (`,`) as the delimiter. Required values must not be blank, and numeric values must be parseable by `BigDecimal`.

### CSV With Header

Pass `true` for `hasHeader`:

```csv
STT,Item,Số lượng,Đơn giá,% VAT,Thành tiền,VAT Amt
1,Laptop,2,1500,10,3000,300
2,Mouse,5,200,10,1000,100
```

Header names are matched after trimming. A UTF-8 BOM at the beginning of a header is ignored. The required columns may be in a different order, but every data row must have the same number of fields as the header row.

### CSV Without Header

Pass `false` for `hasHeader`. Fields must use the following fixed order:

```csv
1,Laptop,2,1500,10,3000,300
2,Mouse,5,200,10,1000,100
```

Each data row must contain exactly seven fields.

Quoted fields are supported, including commas inside a quoted value:

```csv
1,"Laptop, 15 inch",2,1500,10,3000,300
```

Empty lines are ignored. A completely empty CSV produces an empty result through the public API. A header-only CSV is rejected because it contains no data rows. Multi-line quoted fields are not supported.

## Processing Rules

For each input row, the library:

1. Parses the CSV fields into an `InvoiceCsvRow`.
2. Calculates the amount and VAT into an `InvoiceItem`.
3. Preserves the original amount and VAT from the CSV.
4. Aggregates the calculated values into an `InvoiceSummary`.

The library does not enforce a negative-value business rule. Negative numeric values are accepted if they are valid `BigDecimal` values. An application that forbids them must apply that domain rule separately.

## Calculation Formulas

For each row:

```text
calculatedAmount = quantity * unitPrice
calculatedVatAmount = calculatedAmount * normalizedVatRate
```

VAT normalization works as follows:

- `0` remains `0`.
- A rate less than `1` is treated as a decimal rate. For example, `0.10` means 10%.
- A rate greater than or equal to `1` is treated as a percentage and divided by `100`. For example, `10` means 10%.

Examples:

```text
quantity = 2
unitPrice = 1500
VAT = 10

calculatedAmount = 2 * 1500 = 3000
calculatedVatAmount = 3000 * 0.10 = 300
```

## Result Structure

The public API returns an `InvoiceCsvResult` containing:

```java
List<InvoiceItem> items
InvoiceSummary summary
```

Each `InvoiceItem` exposes:

- `stt`
- `item`
- `quantity`
- `unitPrice`
- `vatRate`
- `originalAmount`
- `originalVatAmount`
- `calculatedAmount`
- `calculatedVatAmount`

The original fields are kept separate from calculated fields so callers can compare source values with derived values.

The returned item list is immutable.

## Summary Structure

`InvoiceSummary` exposes:

```java
BigDecimal sumAmount
BigDecimal sumVatAmount
BigDecimal total
```

The values are calculated as:

```text
sumAmount = sum of all calculatedAmount values
sumVatAmount = sum of all calculatedVatAmount values
total = sumAmount + sumVatAmount
```

For empty input, all three summary values are `BigDecimal.ZERO`.

## Public Library Usage

```java
import com.example.invoicecsv.InvoiceCsvLibrary;
import com.example.invoicecsv.model.InvoiceCsvResult;

String csv = """
		STT,Item,Số lượng,Đơn giá,% VAT,Thành tiền,VAT Amt
		1,Laptop,2,1500,10,3000,300
		2,Mouse,5,200,10,1000,100
		""";

InvoiceCsvResult result = new InvoiceCsvLibrary().process(csv, true);

System.out.println(result.getItems().size());
System.out.println(result.getSummary().getSumAmount());
System.out.println(result.getSummary().getSumVatAmount());
System.out.println(result.getSummary().getTotal());
```

For a CSV without a header:

```java
InvoiceCsvResult result = new InvoiceCsvLibrary().process(csvWithoutHeader, false);
```

The library also accepts a `java.io.Reader` or explicit `CsvParseOptions` when a non-default delimiter is required:

```java
CsvParseOptions options = new CsvParseOptions(true, ';');
InvoiceCsvResult result = new InvoiceCsvLibrary().process(reader, options);
```

The library consumes but does not close caller-provided readers. The caller owns the reader and is responsible for closing it.

## Error Handling

The library uses unchecked exceptions:

- `InvoiceCsvException` is the base exception type.
- `CsvValidationException` indicates invalid values, blank required fields, null parser input, or a header without data.
- `MissingColumnException` is a `CsvValidationException` for a missing required header column.
- `CsvFormatException` indicates malformed CSV structure, unterminated quoted fields, incorrect column counts, or reader failures.
- The public API rejects null `String`, `Reader`, and `CsvParseOptions` arguments with `NullPointerException`.

Example:

```java
try {
	InvoiceCsvResult result = new InvoiceCsvLibrary().process(csv, true);
} catch (CsvFormatException | CsvValidationException ex) {
	// Report or handle invalid input.
}
```

## Monetary Precision and Rounding

All numeric values use `BigDecimal`; the library does not use binary floating-point arithmetic.

The default `InvoiceCalculationService` uses `MathContext.DECIMAL64`. Calculations apply that context to amount multiplication and VAT multiplication. The library does not impose a fixed currency scale or a separate monetary rounding policy such as “round to two decimal places.” Values therefore retain the precision produced by `BigDecimal` and the configured math context.

Applications that need a different `MathContext` can construct `InvoiceCalculationService` with their desired context and provide it through the dependency-injection constructor of `InvoiceCsvLibrary`.

## Using the JAR From Another Maven Project

Install the library into the local Maven repository:

```bash
mvn clean install
```

Then add this dependency to the consuming project's `pom.xml`:

```xml
<dependency>
	<groupId>com.example</groupId>
	<artifactId>common-csv</artifactId>
	<version>0.1.0-SNAPSHOT</version>
</dependency>
```

The library has no runtime dependencies. JUnit is test-scoped and is not required by consumers.

## Running Unit Tests

Run the full test suite:

```bash
mvn clean test
```

The tests cover CSV parsing, calculations, summary aggregation, public API processing, exact numeric results, and exception behavior.

## Building the JAR

Build and install the JAR locally:

```bash
mvn clean install
```

The generated artifact is written under `target/` as:

```text
target/common-csv-0.1.0-SNAPSHOT.jar
```

## Project Structure

```text
common_csv_1/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/example/invoicecsv/
│   │   │   ├── InvoiceCsvLibrary.java
│   │   │   ├── exception/
│   │   │   ├── model/
│   │   │   ├── parser/
│   │   │   └── service/
│   │   └── resources/
│   └── test/
│       ├── java/com/example/invoicecsv/
│       └── resources/
└── target/                  # Generated by Maven; not source-controlled
```

## Version Information

- Artifact: `com.example:common-csv`
- Current version: `0.1.0-SNAPSHOT`
- Packaging: `jar`
- Java release: `22`
- Test framework: JUnit Jupiter `5.10.2`

## Limitations and Assumptions

- The default public API expects comma-separated input.
- Header presence must be supplied explicitly by the caller.
- Header-only input is invalid; blank input is valid and produces an empty result.
- Multi-line quoted fields are not supported.
- Required numeric fields must be valid `BigDecimal` values.
- Negative values are not rejected because no such business rule is defined by this library.
- No automatic two-decimal currency rounding is performed.
- The library does not validate whether the source `Thành tiền` or `VAT Amt` values mathematically match the calculated values; it preserves them and calculates separate derived fields.
