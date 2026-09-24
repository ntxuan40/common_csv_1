# API Usage

## Maven dependency

The current artifact is `com.example:common-csv:1.0.0`. Install it locally first:

```bash
mvn clean install
```

Then add it to the consuming Maven project's `pom.xml`:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>common-csv</artifactId>
    <version>1.0.0</version>
</dependency>
```

JUnit is test-scoped and is not required at runtime by consumers.

## Complete processing example

```java
import com.example.invoicecsv.InvoiceCsvLibrary;
import com.example.invoicecsv.model.InvoiceCsvResult;

public final class InvoiceExample {

    public static void main(String[] args) {
        String csv = """
                STT,Item,Số lượng,Đơn giá,% VAT,Thành tiền,VAT Amt
                1,Laptop,2,1500,10,3000,300
                2,Mouse,5,200,10,1000,100
                """;

        InvoiceCsvResult result = new InvoiceCsvLibrary().process(csv, true);

        result.getItems().forEach(item -> {
            System.out.println(item.getItem());
            System.out.println(item.getCalculatedAmount());
            System.out.println(item.getCalculatedVatAmount());
        });

        System.out.println("Amount: " + result.getSummary().getSumAmount());
        System.out.println("VAT: " + result.getSummary().getSumVatAmount());
        System.out.println("Total: " + result.getSummary().getTotal());
    }
}
```

For input without a header, use the same API with `false`:

```java
InvoiceCsvResult result = new InvoiceCsvLibrary().process(csvWithoutHeader, false);
```

## Reader input

Use a `Reader` when the CSV comes from a stream or file:

```java
try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
    InvoiceCsvResult result = new InvoiceCsvLibrary().process(reader, true);
}
```

The library consumes the reader but does not close it; the caller owns the reader and should manage its lifetime as shown above.

The `Reader` overload uses comma separation. For another delimiter, pass explicit options:

```java
CsvParseOptions options = new CsvParseOptions(true, ';');
InvoiceCsvResult result = new InvoiceCsvLibrary().process(reader, options);
```

## Handling failures

The public facade propagates typed CSV exceptions:

```java
try {
    InvoiceCsvResult result = new InvoiceCsvLibrary().process(csv, true);
} catch (CsvFormatException ex) {
    // The CSV structure is malformed.
} catch (CsvValidationException ex) {
    // A required value, number, header, or data condition is invalid.
}
```

Null arguments passed to the public facade are rejected with `NullPointerException`. Empty or whitespace-only CSV is valid and returns an empty item list with zero summary values.

## Custom calculation precision

The facade supports collaborator injection when a consumer needs a custom `MathContext`:

```java
import java.math.MathContext;
import java.math.RoundingMode;

import com.example.invoicecsv.InvoiceCsvLibrary;
import com.example.invoicecsv.model.InvoiceCsvResult;
import com.example.invoicecsv.parser.CsvRowParser;
import com.example.invoicecsv.service.InvoiceCalculationService;
import com.example.invoicecsv.service.InvoiceSummaryService;

InvoiceCalculationService calculationService =
        new InvoiceCalculationService(new MathContext(10, RoundingMode.HALF_UP));

InvoiceCsvLibrary library = new InvoiceCsvLibrary(
        new CsvRowParser(),
        calculationService,
        new InvoiceSummaryService());

InvoiceCsvResult result = library.process(csv, true);
```
