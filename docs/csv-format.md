# CSV Format

## Required columns

The logical columns are:

| Column | Type | Description |
| --- | --- | --- |
| `STT` | Text | Source row number or identifier |
| `Item` | Text | Item name or description |
| `Số lượng` | `BigDecimal` | Quantity |
| `Đơn giá` | `BigDecimal` | Unit price |
| `% VAT` | `BigDecimal` | VAT as percent or decimal rate |

The default delimiter is comma (`,`). A custom delimiter can be supplied through `CsvParseOptions`.

## Header input

The caller must pass `hasHeader = true` when the first row contains column names:

```csv
STT,Item,Số lượng,Đơn giá,% VAT
1,Laptop,2,1500,10,3000,300
```

Required header names are matched after trimming. A UTF-8 BOM at the start of a header is ignored. Required columns may be reordered. Every data row must have exactly the same number of fields as the header row.

A header-only input is invalid because there is no data row to process.

## No-header input

The caller must pass `hasHeader = false` when rows are positional. The fixed order is:

```csv
STT,Item,Số lượng,Đơn giá,% VAT
```

The header text above is descriptive only; it must not be included in the actual no-header input:

```csv
1,Laptop,2,1500,10,3000,300
2,Mouse,5,200,10,1000,100
```

Each row must contain exactly seven fields.

## Values and quoting

- `STT`, `Item`, `Số lượng`, `Đơn giá`, and `% VAT` must not be blank.
- Numeric values are parsed with `new BigDecimal(...)`.
- Negative numeric values are accepted; the library does not impose a negative-value business rule.
- Quoted values are supported.
- A comma inside a quoted value is treated as part of that value.
- Two adjacent double quotes inside a quoted value represent one literal double quote.
- Blank lines are ignored.
- Multiline quoted fields are not supported.

Example quoted value:

```csv
1,"Laptop, 15 inch",2,1500,10,3000,300
```

## Invalid input behavior

- Missing required header: `MissingColumnException`
- Blank required value: `CsvValidationException`
- Invalid numeric value: `CsvValidationException`
- Header-only input: `CsvValidationException`
- Unterminated quoted field: `CsvFormatException`
- Incorrect column count: `CsvFormatException`
- Empty or whitespace-only CSV: valid; it produces an empty result
