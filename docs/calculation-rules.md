# Calculation Rules

## Row calculations

For each parsed `InvoiceCsvRow`, `InvoiceCalculationService` calculates:

```text
calculatedAmount = quantity * unitPrice
calculatedVatAmount = calculatedAmount * normalizedVatRate
```

The original `Thành tiền` and `VAT Amt` values are retained separately. Either source field may be blank; in that case the corresponding original model value is `null`. The library does not compare source values with calculated values or overwrite them.

## VAT normalization

VAT values support both common representations:

| Input | Interpretation | Normalized rate |
| --- | --- | --- |
| `0` | Zero percent | `0` |
| `0.10` | Decimal rate | `0.10` |
| `10` | Percent | `0.10` |
| `20` | Percent | `0.20` |

The implementation treats a non-zero rate below `1` as a decimal rate. A rate greater than or equal to `1` is divided by `100`.

Example:

```text
quantity = 2
unitPrice = 1500
VAT = 10

calculatedAmount = 2 * 1500 = 3000
calculatedVatAmount = 3000 * (10 / 100) = 300
```

## Summary calculations

`InvoiceSummaryService` aggregates calculated fields only:

```text
sumAmount = sum(calculatedAmount for every item)
sumVatAmount = sum(calculatedVatAmount for every item)
total = sumAmount + sumVatAmount
```

For an empty item list:

```text
sumAmount = 0
sumVatAmount = 0
total = 0
```

## Precision and rounding

All numeric values use `BigDecimal`. The default `InvoiceCalculationService` uses `MathContext.DECIMAL64` for amount multiplication, VAT-rate normalization division, and VAT multiplication.

There is no fixed two-decimal currency scale and no separate currency-rounding policy. Results retain the precision produced by `BigDecimal` and the configured math context. A consumer can construct `InvoiceCalculationService` with another `MathContext` and inject it into `InvoiceCsvLibrary` through the collaborator constructor.

Summary addition does not apply another `MathContext`; it adds the already calculated `BigDecimal` values directly.
