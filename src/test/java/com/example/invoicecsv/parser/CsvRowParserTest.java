package com.example.invoicecsv.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.invoicecsv.exception.CsvFormatException;
import com.example.invoicecsv.exception.CsvValidationException;
import com.example.invoicecsv.exception.MissingColumnException;
import com.example.invoicecsv.model.InvoiceCsvRow;

class CsvRowParserTest {

    private final CsvRowParser parser = new CsvRowParser();

    @Test
    void givenHeaderCsv_whenParsed_thenAllFieldsAreMappedExactly() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT,Thành tiền,VAT Amt",
                "1,Laptop,2,1500,10,3000,300");

        List<InvoiceCsvRow> rows = parser.parse(csv, new CsvParseOptions(true, ','));

        assertEquals(1, rows.size());
        assertEquals("1", rows.get(0).getStt());
        assertEquals("Laptop", rows.get(0).getItem());
        assertMoneyEquals("2", rows.get(0).getQuantity());
        assertMoneyEquals("1500", rows.get(0).getUnitPrice());
        assertMoneyEquals("10", rows.get(0).getVatRate());
        assertMoneyEquals("3000", rows.get(0).getOriginalAmount());
        assertMoneyEquals("300", rows.get(0).getOriginalVatAmount());
    }

    @Test
    void givenCsvWithoutHeader_whenParsed_thenRowsUseFixedColumnOrder() {
        String csv = String.join(System.lineSeparator(),
                "1,Laptop,2,1500,10,3000,300",
                "2,Mouse,5,200,10,1000,100");

        List<InvoiceCsvRow> rows = parser.parse(csv, new CsvParseOptions(false, ','));

        assertEquals(2, rows.size());
        assertEquals("Laptop", rows.get(0).getItem());
        assertEquals("Mouse", rows.get(1).getItem());
    }

    @Test
    void givenReorderedHeader_whenParsed_thenColumnsAreResolvedByName() {
        String csv = String.join(System.lineSeparator(),
                "Item,STT,% VAT,VAT Amt,Đơn giá,Thành tiền,Số lượng",
                "Laptop,1,10,300,1500,3000,2");

        List<InvoiceCsvRow> rows = parser.parse(csv, new CsvParseOptions(true, ','));

        assertEquals("1", rows.get(0).getStt());
        assertEquals("Laptop", rows.get(0).getItem());
        assertMoneyEquals("2", rows.get(0).getQuantity());
        assertMoneyEquals("1500", rows.get(0).getUnitPrice());
    }

    @Test
    void givenUtf8BomInHeader_whenParsed_thenRequiredColumnIsRecognized() {
        String csv = String.join(System.lineSeparator(),
                "\uFEFFSTT,Item,Số lượng,Đơn giá,% VAT,Thành tiền,VAT Amt",
                "1,Laptop,2,1500,10,3000,300");

        List<InvoiceCsvRow> rows = parser.parse(csv, new CsvParseOptions(true, ','));

        assertEquals(1, rows.size());
        assertEquals("1", rows.get(0).getStt());
    }

    @Test
    void givenCustomDelimiter_whenParsed_thenFieldsAreSeparatedUsingConfiguredDelimiter() {
        String csv = "1;Laptop;2;1500;10;3000;300";

        List<InvoiceCsvRow> rows = parser.parse(csv, new CsvParseOptions(false, ';'));

        assertEquals(1, rows.size());
        assertEquals("Laptop", rows.get(0).getItem());
        assertMoneyEquals("3000", rows.get(0).getOriginalAmount());
    }

    @Test
    void givenNullString_whenParsed_thenValidationExceptionIsThrown() {
        assertThrows(CsvValidationException.class, () -> parser.parse((String) null, new CsvParseOptions(true, ',')));
    }

    @Test
    void givenNullReader_whenParsed_thenValidationExceptionIsThrown() {
        assertThrows(CsvValidationException.class, () -> parser.parse((java.io.Reader) null, new CsvParseOptions(true, ',')));
    }

    @Test
    void givenEmptyCsv_whenParsed_thenEmptyListIsReturned() {
        assertTrue(parser.parse("", new CsvParseOptions(true, ',')).isEmpty());
    }

    @Test
    void givenHeaderOnlyCsv_whenParsed_thenValidationExceptionIsThrown() {
        String csv = "STT,Item,Số lượng,Đơn giá,% VAT,Thành tiền,VAT Amt";

        assertThrows(CsvValidationException.class, () -> parser.parse(csv, new CsvParseOptions(true, ',')));
    }

    @Test
    void givenMissingRequiredHeader_whenParsed_thenMissingColumnExceptionIsThrown() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,Thành tiền",
                "1,Laptop,2,1500,3000");

        assertThrows(MissingColumnException.class, () -> parser.parse(csv, new CsvParseOptions(true, ',')));
    }

    @Test
    void givenIncorrectColumnCount_whenParsed_thenFormatExceptionIsThrown() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT,Thành tiền,VAT Amt",
                "1,Laptop,2,1500,10,3000");

        assertThrows(CsvFormatException.class, () -> parser.parse(csv, new CsvParseOptions(true, ',')));
    }

    @Test
    void givenInvalidNumericValue_whenParsed_thenValidationExceptionIsThrown() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT,Thành tiền,VAT Amt",
                "1,Laptop,abc,1500,10,3000,300");

        assertThrows(CsvValidationException.class, () -> parser.parse(csv, new CsvParseOptions(true, ',')));
    }

    @Test
    void givenQuotedValue_whenParsed_thenQuotesAreRemovedAndValueIsPreserved() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT,Thành tiền,VAT Amt",
                "1,\"Laptop Pro 15\",2,1500,10,3000,300");

        List<InvoiceCsvRow> rows = parser.parse(csv, new CsvParseOptions(true, ','));

        assertEquals("Laptop Pro 15", rows.get(0).getItem());
    }

    @Test
    void givenEscapedQuote_whenParsed_thenLiteralQuoteIsPreserved() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT,Thành tiền,VAT Amt",
                "1,\"Laptop \"\"Pro\"\"\",2,1500,10,3000,300");

        List<InvoiceCsvRow> rows = parser.parse(csv, new CsvParseOptions(true, ','));

        assertEquals("Laptop \"Pro\"", rows.get(0).getItem());
    }

    @Test
    void givenQuoteInsideUnquotedValue_whenParsed_thenFormatExceptionIsThrown() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT,Thành tiền,VAT Amt",
                "1,Lap\"top,2,1500,10,3000,300");

        assertThrows(CsvFormatException.class, () -> parser.parse(csv, new CsvParseOptions(true, ',')));
    }

    @Test
    void givenCharacterAfterClosingQuote_whenParsed_thenFormatExceptionIsThrown() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT,Thành tiền,VAT Amt",
                "1,\"Laptop\"Pro,2,1500,10,3000,300");

        assertThrows(CsvFormatException.class, () -> parser.parse(csv, new CsvParseOptions(true, ',')));
    }

    @Test
    void givenReader_whenParsed_thenCallerOwnedReaderRemainsOpen() {
        TrackingReader reader = new TrackingReader("1,Laptop,2,1500,10,3000,300");

        parser.parse(reader, new CsvParseOptions(false, ','));

        assertTrue(!reader.isClosed());
    }

    @Test
    void givenMissingRequiredValue_whenParsed_thenValidationExceptionIsThrown() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT,Thành tiền,VAT Amt",
                "1,,2,1500,10,3000,300");

        assertThrows(CsvValidationException.class, () -> parser.parse(csv, new CsvParseOptions(true, ',')));
    }

    @Test
    void givenMalformedCsv_whenParsed_thenFormatExceptionIsThrown() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT,Thành tiền,VAT Amt",
                "1,\"Laptop,2,1500,10,3000,300");

        assertThrows(CsvFormatException.class, () -> parser.parse(csv, new CsvParseOptions(true, ',')));
    }

    private void assertMoneyEquals(String expected, java.math.BigDecimal actual) {
        assertEquals(0, actual.compareTo(new java.math.BigDecimal(expected)),
                () -> "Expected value " + expected + " but was " + actual.toPlainString());
    }

    private static final class TrackingReader extends StringReader {

        private boolean closed;

        private TrackingReader(String value) {
            super(value);
        }

        @Override
        public void close() {
            closed = true;
        }

        private boolean isClosed() {
            return closed;
        }
    }
}
