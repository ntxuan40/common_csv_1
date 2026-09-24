package com.example.invoicecsv.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.math.BigDecimal;
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
                "STT,Item,Số lượng,Đơn giá,% VAT",
                "1,Laptop,2,1500,10");

        List<InvoiceCsvRow> rows = parser.parse(csv, new CsvParseOptions(true, ','));

        assertEquals(1, rows.size());
        assertEquals("1", rows.get(0).getStt());
        assertEquals("Laptop", rows.get(0).getItem());
        assertMoneyEquals("2", rows.get(0).getQuantity());
        assertMoneyEquals("1500", rows.get(0).getUnitPrice());
        assertMoneyEquals("10", rows.get(0).getVatRate());
    }

    @Test
    void givenCsvWithoutHeader_whenParsed_thenRowsUseFixedColumnOrder() {
        String csv = String.join(System.lineSeparator(),
                "1,Laptop,2,1500,10",
                "2,Mouse,5,200,10");

        List<InvoiceCsvRow> rows = parser.parse(csv, new CsvParseOptions(false, ','));

        assertEquals(2, rows.size());
        assertEquals("Laptop", rows.get(0).getItem());
        assertEquals("Mouse", rows.get(1).getItem());
    }

    @Test
    void givenReorderedHeader_whenParsed_thenColumnsAreResolvedByName() {
        String csv = String.join(System.lineSeparator(),
                "Item,STT,% VAT,Đơn giá,Số lượng",
                "Laptop,1,10,1500,2");

        List<InvoiceCsvRow> rows = parser.parse(csv, new CsvParseOptions(true, ','));

        assertEquals("1", rows.get(0).getStt());
        assertEquals("Laptop", rows.get(0).getItem());
        assertMoneyEquals("2", rows.get(0).getQuantity());
        assertMoneyEquals("1500", rows.get(0).getUnitPrice());
    }

    @Test
    void givenUtf8BomInHeader_whenParsed_thenRequiredColumnIsRecognized() {
        String csv = String.join(System.lineSeparator(),
                "\uFEFFSTT,Item,Số lượng,Đơn giá,% VAT",
                "1,Laptop,2,1500,10");

        List<InvoiceCsvRow> rows = parser.parse(csv, new CsvParseOptions(true, ','));

        assertEquals(1, rows.size());
        assertEquals("1", rows.get(0).getStt());
    }

    @Test
    void givenCustomDelimiter_whenParsed_thenFieldsAreSeparatedUsingConfiguredDelimiter() {
        String csv = "1;Laptop;2;1500;10";

        List<InvoiceCsvRow> rows = parser.parse(csv, new CsvParseOptions(false, ';'));

        assertEquals(1, rows.size());
        assertEquals("Laptop", rows.get(0).getItem());
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
        String csv = "STT,Item,Số lượng,Đơn giá,% VAT";

        assertThrows(CsvValidationException.class, () -> parser.parse(csv, new CsvParseOptions(true, ',')));
    }

    @Test
    void givenMissingRequiredHeader_whenParsed_thenMissingColumnExceptionIsThrown() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá",
                "1,Laptop,2,1500");

        assertThrows(MissingColumnException.class, () -> parser.parse(csv, new CsvParseOptions(true, ',')));
    }

    @Test
    void givenIncorrectColumnCount_whenParsed_thenFormatExceptionIsThrown() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT",
                "1,Laptop,2,1500");

        assertThrows(CsvFormatException.class, () -> parser.parse(csv, new CsvParseOptions(true, ',')));
    }

    @Test
    void givenInvalidNumericValue_whenParsed_thenValidationExceptionIsThrown() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT",
                "1,Laptop,abc,1500,10");

        assertThrows(CsvValidationException.class, () -> parser.parse(csv, new CsvParseOptions(true, ',')));
    }

    @Test
    void givenQuotedValue_whenParsed_thenQuotesAreRemovedAndValueIsPreserved() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT",
                "1,\"Laptop Pro 15\",2,1500,10");

        List<InvoiceCsvRow> rows = parser.parse(csv, new CsvParseOptions(true, ','));

        assertEquals("Laptop Pro 15", rows.get(0).getItem());
    }

    @Test
    void givenEscapedQuote_whenParsed_thenLiteralQuoteIsPreserved() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT",
                "1,\"Laptop \"\"Pro\"\"\",2,1500,10");

        List<InvoiceCsvRow> rows = parser.parse(csv, new CsvParseOptions(true, ','));

        assertEquals("Laptop \"Pro\"", rows.get(0).getItem());
    }

    @Test
    void givenQuoteInsideUnquotedValue_whenParsed_thenFormatExceptionIsThrown() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT",
                "1,Lap\"top,2,1500,10");

        assertThrows(CsvFormatException.class, () -> parser.parse(csv, new CsvParseOptions(true, ',')));
    }

    @Test
    void givenCharacterAfterClosingQuote_whenParsed_thenFormatExceptionIsThrown() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT",
                "1,\"Laptop\"Pro,2,1500,10");

        assertThrows(CsvFormatException.class, () -> parser.parse(csv, new CsvParseOptions(true, ',')));
    }

    @Test
    void givenReader_whenParsed_thenCallerOwnedReaderRemainsOpen() {
        TrackingReader reader = new TrackingReader("1,Laptop,2,1500,10");

        parser.parse(reader, new CsvParseOptions(false, ','));

        assertTrue(!reader.isClosed());
    }

    @Test
    void givenMissingRequiredValue_whenParsed_thenValidationExceptionIsThrown() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT",
                "1,,2,1500,10");

        assertThrows(CsvValidationException.class, () -> parser.parse(csv, new CsvParseOptions(true, ',')));
    }

    @Test
    void givenMalformedCsv_whenParsed_thenFormatExceptionIsThrown() {
        String csv = String.join(System.lineSeparator(),
                "STT,Item,Số lượng,Đơn giá,% VAT",
                "1,\"Laptop,2,1500,10");

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
    
    @Test
    void testEqualsAndHashCodeAndToString() {
        // 1. Khởi tạo các đối tượng để test
        InvoiceCsvRow row1 = new InvoiceCsvRow("1", "Item A", BigDecimal.TEN, BigDecimal.valueOf(100), BigDecimal.valueOf(0.1));
        InvoiceCsvRow row2 = new InvoiceCsvRow("1", "Item A", BigDecimal.TEN, BigDecimal.valueOf(100), BigDecimal.valueOf(0.1));
        InvoiceCsvRow row3 = new InvoiceCsvRow("2", "Item B", BigDecimal.ONE, BigDecimal.valueOf(200), BigDecimal.valueOf(0.1));

        // 2. Test hàm equals() để phủ hết các nhánh màu đỏ
        assertTrue(row1.equals(row1));          // Phủ nhánh: this == obj
        assertFalse(row1.equals(null));         // Phủ nhánh: obj == null
        assertFalse(row1.equals("NotARow"));    // Phủ nhánh: khác kiểu dữ liệu
        assertTrue(row1.equals(row2));          // Phủ nhánh: các thuộc tính giống nhau
        assertFalse(row1.equals(row3));         // Phủ nhánh: các thuộc tính khác nhau

        // 3. Test hàm hashCode()
        assertEquals(row1.hashCode(), row2.hashCode());
        assertNotEquals(row1.hashCode(), row3.hashCode());

        // 4. Test hàm toString()
        assertNotNull(row1.toString());
        assertTrue(row1.toString().contains("Item A")); // Kiểm tra xem có chứa tên item không
    }
    
    @Test
    void testInvoiceCsvRowRequireNonBlankThrowsException() {
        // Phủ dòng 99-100: Test trường hợp truyền chuỗi trống/null vào constructor để kích hoạt Exception
        assertThrows(IllegalArgumentException.class, () -> {
            new InvoiceCsvRow("", "Item A", BigDecimal.TEN, BigDecimal.valueOf(100), BigDecimal.valueOf(0.1));
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new InvoiceCsvRow("1", "   ", BigDecimal.TEN, BigDecimal.valueOf(100), BigDecimal.valueOf(0.1));
        });
    }

    @Test
    void testInvoiceCsvRowEqualsFullBranches() {
        InvoiceCsvRow base = new InvoiceCsvRow("1", "Item A", BigDecimal.TEN, BigDecimal.valueOf(100), BigDecimal.valueOf(0.1));
        
        // Phủ các nhánh màu vàng ở hàm equals (dòng 117-121) bằng cách cho lệch từng thuộc tính một
        InvoiceCsvRow diffStt = new InvoiceCsvRow("2", "Item A", BigDecimal.TEN, BigDecimal.valueOf(100), BigDecimal.valueOf(0.1));
        InvoiceCsvRow diffItem = new InvoiceCsvRow("1", "Item B", BigDecimal.TEN, BigDecimal.valueOf(100), BigDecimal.valueOf(0.1));
        InvoiceCsvRow diffQty = new InvoiceCsvRow("1", "Item A", BigDecimal.ONE, BigDecimal.valueOf(100), BigDecimal.valueOf(0.1));
        InvoiceCsvRow diffPrice = new InvoiceCsvRow("1", "Item A", BigDecimal.TEN, BigDecimal.valueOf(200), BigDecimal.valueOf(0.1));
        InvoiceCsvRow diffVat = new InvoiceCsvRow("1", "Item A", BigDecimal.TEN, BigDecimal.valueOf(100), BigDecimal.valueOf(0.2));

        assertFalse(base.equals(diffStt));
        assertFalse(base.equals(diffItem));
        assertFalse(base.equals(diffQty));
        assertFalse(base.equals(diffPrice));
        assertFalse(base.equals(diffVat));
    }
    
    @Test
    void testParseWithOptionsNullThrowsException() {
        // 1. Phủ dòng 48-49: Truyền options là null để kích hoạt Exception
        java.io.StringReader reader = new java.io.StringReader("stt,item\n1,Item A");
        assertThrows(CsvValidationException.class, () -> {
            new CsvRowParser().parse(reader, null);
        });
    }

    @Test
    void testParseWithEmptyLinesReturnsEmptyList() {
        // 2. Phủ dòng 55-57: Truyền chuỗi trống hoặc chỉ có dòng trống để lines.isEmpty() đúng
        java.io.StringReader reader = new java.io.StringReader("");
        CsvParseOptions options = CsvParseOptions.defaults();

        List<InvoiceCsvRow> result = new CsvRowParser().parse(reader, options);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseWithIOExceptionThrowsFormatException() throws java.io.IOException {
        // 3. Phủ dòng 64-65: Giả lập một Reader bị lỗi khi đọc để kích hoạt IOException
        java.io.Reader brokenReader = new java.io.Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws java.io.IOException {
                throw new java.io.IOException("Giả lập lỗi phần cứng/đọc file");
            }
            @Override
            public void close() throws java.io.IOException {}
        };
        
        CsvParseOptions options = CsvParseOptions.defaults();

        assertThrows(CsvFormatException.class, () -> {
            new CsvRowParser().parse(brokenReader, options);
        });
    }
    
    
    
    
}
