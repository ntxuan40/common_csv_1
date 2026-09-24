package com.example.invoicecsv.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.example.invoicecsv.exception.CsvFormatException;
import com.example.invoicecsv.exception.CsvValidationException;
import com.example.invoicecsv.exception.MissingColumnException;
import com.example.invoicecsv.model.InvoiceCsvRow;

/**
 * Parses CSV input into typed {@link InvoiceCsvRow} objects.
 *
 * <p>This parser is responsible only for converting CSV text into domain input rows. It does not perform
 * any calculation or summary logic.</p>
 */
public class CsvRowParser {

    private static final String[] REQUIRED_COLUMNS = {
            "STT",
            "Item",
            "Số lượng",
            "Đơn giá",
            "% VAT"
    };

        /**
         * Creates a parser using parse options supplied at invocation time.
         */
        public CsvRowParser() {
        }

    /**
     * Parses CSV content from a reader using the supplied options.
     *
     * @param reader the source CSV input
     * @param options parsing behavior including header handling
     * @return parsed rows as domain objects
     */
    public List<InvoiceCsvRow> parse(Reader reader, CsvParseOptions options) {
        if (reader == null) {
            throw new CsvValidationException("CSV reader must not be null");
        }
        if (options == null) {
            throw new CsvValidationException("CSV parse options must not be null");
        }

        BufferedReader bufferedReader = reader instanceof BufferedReader existingReader
            ? existingReader
            : new BufferedReader(reader);
        try {
            List<String> lines = readNonEmptyLines(bufferedReader);
            if (lines.isEmpty()) {
                return List.of();
            }

            List<String[]> records = parseRecords(lines, options.getDelimiter());
            if (options.hasHeader()) {
                return parseWithHeader(records);
            }
            return parseWithoutHeader(records);
        } catch (IOException e) {
            throw new CsvFormatException("Unable to read CSV content: " + e.getMessage(), e);
        }
    }

    /**
     * Parses CSV content from a string using the supplied options.
     *
     * @param csv the CSV content to parse
     * @param options parsing behavior including header handling
     * @return parsed rows as domain objects
     */
    public List<InvoiceCsvRow> parse(String csv, CsvParseOptions options) {
        if (csv == null) {
            throw new CsvValidationException("CSV input must not be null");
        }
        if (csv.isBlank()) {
            return List.of();
        }
        return parse(new java.io.StringReader(csv), options);
    }

    private List<String> readNonEmptyLines(BufferedReader reader) throws IOException {
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                lines.add(line);
            }
        }
        return lines;
    }

    private List<String[]> parseRecords(List<String> lines, char delimiter) {
        List<String[]> records = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] tokens = splitCsvLine(line, delimiter, i + 1);
            records.add(tokens);
        }
        return records;
    }

    private String[] splitCsvLine(String line, char delimiter, int lineNumber) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        boolean fieldStarted = false;
        boolean closedQuote = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes) {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                        closedQuote = true;
                    }
                } else if (fieldStarted || closedQuote) {
                    throw new CsvFormatException("Malformed CSV: unexpected quote at line " + lineNumber);
                } else {
                    inQuotes = true;
                    fieldStarted = true;
                }
            } else if (ch == delimiter && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
                fieldStarted = false;
                closedQuote = false;
            } else if (closedQuote) {
                throw new CsvFormatException("Malformed CSV: unexpected character after closing quote at line "
                        + lineNumber);
            } else {
                current.append(ch);
                fieldStarted = true;
            }
        }

        if (inQuotes) {
            throw new CsvFormatException("Malformed CSV: unterminated quoted field at line " + lineNumber);
        }

        result.add(current.toString());
        return result.toArray(String[]::new);
    }

    private List<InvoiceCsvRow> parseWithHeader(List<String[]> records) {
        if (records.size() == 1) {
            throw new CsvValidationException("CSV contains only a header row and no data rows");
        }

        String[] header = records.get(0);
        List<Integer> columnIndexes = resolveRequiredColumnIndexes(header);

        List<InvoiceCsvRow> rows = new ArrayList<>();
        for (int i = 1; i < records.size(); i++) {
            String[] row = records.get(i);
            validateRowLength(row, header.length, i + 1);
            rows.add(mapRow(row, columnIndexes, i + 1));
        }
        return rows;
    }

    private List<InvoiceCsvRow> parseWithoutHeader(List<String[]> records) {
        if (records.isEmpty()) {
            return List.of();
        }

        List<InvoiceCsvRow> rows = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            String[] row = records.get(i);
            validateRowLength(row, REQUIRED_COLUMNS.length, i + 1);
            rows.add(mapRow(row, getFixedColumnIndexes(), i + 1));
        }
        return rows;
    }

    private List<Integer> resolveRequiredColumnIndexes(String[] header) {
        List<Integer> indexes = new ArrayList<>();
        for (String required : REQUIRED_COLUMNS) {
            boolean found = false;
            for (int i = 0; i < header.length; i++) {
                if (normalizeHeader(header[i]).equals(normalizeHeader(required))) {
                    indexes.add(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new MissingColumnException("Missing required column: " + required);
            }
        }
        return indexes;
    }

    private List<Integer> getFixedColumnIndexes() {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < REQUIRED_COLUMNS.length; i++) {
            indexes.add(i);
        }
        return indexes;
    }

    private InvoiceCsvRow mapRow(String[] row, List<Integer> indexes, int lineNumber) {
        String stt = getValue(row, indexes.get(0), lineNumber, "STT");
        String item = getValue(row, indexes.get(1), lineNumber, "Item");
        String quantityText = getValue(row, indexes.get(2), lineNumber, "Số lượng");
        String unitPriceText = getValue(row, indexes.get(3), lineNumber, "Đơn giá");
        String vatRateText = getValue(row, indexes.get(4), lineNumber, "% VAT");
        return new InvoiceCsvRow(
                stt,
                item,
                parseBigDecimal(quantityText, "Số lượng", lineNumber),
                parseBigDecimal(unitPriceText, "Đơn giá", lineNumber),
                parseBigDecimal(vatRateText, "% VAT", lineNumber));
    }

    private String getValue(String[] row, int index, int lineNumber, String fieldName) {
        if (index >= row.length) {
            throw new CsvValidationException("Missing value for column '" + fieldName + "' at line " + lineNumber);
        }
        String value = row[index].trim();
        if (value.isEmpty()) {
            throw new CsvValidationException("Empty required value for column '" + fieldName + "' at line " + lineNumber);
        }
        return value;
    }

    private BigDecimal parseBigDecimal(String text, String fieldName, int lineNumber) {
        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            throw new CsvValidationException(
                    "Invalid numeric value for column '" + fieldName + "' at line " + lineNumber + ": '" + text + "'");
        }
    }

    private void validateRowLength(String[] row, int expectedLength, int lineNumber) {
        if (row.length != expectedLength) {
            throw new CsvFormatException(
                    "Invalid column count at line " + lineNumber + ": expected " + expectedLength + " columns but found " + row.length);
        }
    }

    private String normalizeHeader(String header) {
        return header == null ? "" : header.trim().replace("\uFEFF", "");
    }
}
