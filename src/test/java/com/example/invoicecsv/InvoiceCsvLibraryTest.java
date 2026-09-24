package com.example.invoicecsv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.invoicecsv.exception.CsvFormatException;
import com.example.invoicecsv.exception.CsvValidationException;
import com.example.invoicecsv.exception.InvoiceCsvException;
import com.example.invoicecsv.model.InvoiceCsvResult;
import com.example.invoicecsv.model.InvoiceItem;
import com.example.invoicecsv.model.InvoiceSummary;
import com.example.invoicecsv.parser.CsvParseOptions;

class InvoiceCsvLibraryTest {

    @Test
    void givenEmptyCsv_whenProcessed_thenEmptyResultHasZeroSummary() {
        InvoiceCsvResult result = new InvoiceCsvLibrary().process("", true);

        assertEquals(0, result.getItems().size());
        assertMoneyEquals("0", result.getSummary().getSumAmount());
        assertMoneyEquals("0", result.getSummary().getSumVatAmount());
        assertMoneyEquals("0", result.getSummary().getTotal());
    }

    private void assertMoneyEquals(String expected, BigDecimal actual) {
        assertEquals(0, actual.compareTo(new BigDecimal(expected)),
                () -> "Expected value " + expected + " but was " + actual.toPlainString());
    }
    @Test
    void testInvoiceCsvResultEqualsHashCodeAndToString() {
        // 1. Khởi tạo các danh sách và đối tượng InvoiceSummary dummy bằng hàm factory '.of'
        List<InvoiceItem> list1 = List.of();
        List<InvoiceItem> list2 = List.of();
        
        InvoiceSummary summary1 = InvoiceSummary.of(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        InvoiceSummary summary2 = InvoiceSummary.of(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        InvoiceSummary summary3 = InvoiceSummary.of(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN);

        // 2. Khởi tạo các đối tượng InvoiceCsvResult để thực hiện test so sánh
        InvoiceCsvResult result1 = new InvoiceCsvResult(list1, summary1);
        InvoiceCsvResult result2 = new InvoiceCsvResult(list2, summary2);
        InvoiceCsvResult result3 = new InvoiceCsvResult(list1, summary3);

        // 3. Phủ code hoàn toàn cho hàm equals() thông qua các nhánh điều kiện
        assertTrue(result1.equals(result1));          // Nhánh: this == obj
        assertFalse(result1.equals(null));         // Nhánh: obj == null
        assertFalse(result1.equals("NotAResult"));  // Nhánh: khác kiểu dữ liệu
        assertTrue(result1.equals(result2));          // Nhánh: các thuộc tính giống nhau hoàn toàn
        assertFalse(result1.equals(result3));         // Nhánh: thuộc tính khác nhau

        // 4. Phủ code cho hàm hashCode()
        assertEquals(result1.hashCode(), result2.hashCode());
        assertNotEquals(result1.hashCode(), result3.hashCode());

        // 5. Phủ code cho hàm toString()
        assertNotNull(result1.toString());
        assertTrue(result1.toString().contains("InvoiceCsvResult"));
    }
    
    @Test
    void testProcessWithNullInputThrowsException() {
        // Khởi tạo một option dummy để truyền vào hàm
        CsvParseOptions options = new CsvParseOptions(true, ','); 
        // Lưu ý: Nếu constructor của CsvParseOptions của bạn khác, hãy sửa lại cho đúng.
        // Hoặc nếu lớp này có hàm builder/factory như CsvParseOptions.defaultOptions() thì dùng nó.

        // Phủ dòng 83: Test trường hợp csvInput truyền vào là null để kích hoạt NullPointerException
        assertThrows(NullPointerException.class, () -> {
            new InvoiceCsvLibrary().process((String) null, options);
        });
    }
    
    @Test
    void testCsvParseOptionsMethods() {
        // 1. Phủ code cho hàm static defaults() (Dòng 47)
        CsvParseOptions defaultOptions = CsvParseOptions.defaults();
        assertNotNull(defaultOptions);
        assertTrue(defaultOptions.hasHeader());
        assertEquals(',', defaultOptions.getDelimiter());

        // 2. Khởi tạo các đối tượng để test hàm equals, hashCode
        CsvParseOptions options1 = new CsvParseOptions(true, ',');
        CsvParseOptions options2 = new CsvParseOptions(true, ',');
        CsvParseOptions options3 = new CsvParseOptions(false, ';');
        // Lưu ý: Nếu constructor của bạn nhận 3 tham số, hãy sửa lại cho đúng 
        // ví dụ: new CsvParseOptions(true, ',', '"') dựa theo hàm defaults() của bạn.

        // 3. Phủ code hoàn toàn cho hàm equals() (Dòng 51 -> 60)
        assertTrue(options1.equals(options1));          // Nhánh: this == obj
        assertFalse(options1.equals(null));         // Nhánh: obj == null
        assertFalse(options1.equals("NotAnOption")); // Nhánh: khác kiểu dữ liệu
        assertTrue(options1.equals(options2));          // Nhánh: các thuộc tính giống nhau
        assertFalse(options1.equals(options3));         // Nhánh: các thuộc tính khác nhau

        // 4. Phủ code cho hàm hashCode() (Dòng 62)
        assertEquals(options1.hashCode(), options2.hashCode());
        assertNotEquals(options1.hashCode(), options3.hashCode());

        // 5. Phủ code cho hàm toString() (Dòng 67)
        assertNotNull(options1.toString());
        assertTrue(options1.toString().contains("CsvParseOptions"));
    }
    
    @Test
    void testExceptionConstructors() {
        Throwable cause = new RuntimeException("Gốc lỗi");
        String errMsg = "Thông báo lỗi kiểm thử";

        // 1. Phủ code cho CsvFormatException
        CsvFormatException exFormat = new CsvFormatException(errMsg, cause);
        assertEquals(errMsg, exFormat.getMessage());
        assertEquals(cause, exFormat.getCause());

        // 2. Phủ code cho CsvValidationException
        CsvValidationException exValid = new CsvValidationException(errMsg, cause);
        assertEquals(errMsg, exValid.getMessage());
        assertEquals(cause, exValid.getCause());

        // 3. Phủ code cho InvoiceCsvException
        InvoiceCsvException exInvoice = new InvoiceCsvException(errMsg, cause);
        assertEquals(errMsg, exInvoice.getMessage());
        assertEquals(cause, exInvoice.getCause());
    }
    
}
