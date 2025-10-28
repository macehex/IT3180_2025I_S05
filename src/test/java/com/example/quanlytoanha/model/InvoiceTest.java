package com.example.quanlytoanha.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Unit tests for Invoice class
 */
public class InvoiceTest {

    private Invoice invoice;

    @BeforeEach
    public void setUp() {
        invoice = new Invoice();
    }

    @Test
    @DisplayName("Should create empty invoice with default constructor")
    public void testDefaultConstructor() {
        // Then
        assertNotNull(invoice);
        assertNotNull(invoice.getDetails());
        assertEquals(0, invoice.getDetails().size());
    }

    @Test
    @DisplayName("Should create invoice with parameterized constructor")
    public void testParameterizedConstructor() {
        // Given
        int invoiceId = 1;
        BigDecimal totalAmount = new BigDecimal("150.50");
        Date dueDate = new Date();

        // When
        Invoice paramInvoice = new Invoice(invoiceId, totalAmount, dueDate);

        // Then
        assertEquals(invoiceId, paramInvoice.getInvoiceId());
        assertEquals(totalAmount, paramInvoice.getTotalAmount());
        assertEquals(dueDate, paramInvoice.getDueDate());
        assertNotNull(paramInvoice.getDetails());
        assertEquals(0, paramInvoice.getDetails().size());
    }

    @Test
    @DisplayName("Should set and get invoice ID")
    public void testInvoiceId() {
        // When
        invoice.setInvoiceId(123);

        // Then
        assertEquals(123, invoice.getInvoiceId());
    }

    @Test
    @DisplayName("Should set and get total amount")
    public void testTotalAmount() {
        // Given
        BigDecimal amount = new BigDecimal("250.75");

        // When
        invoice.setTotalAmount(amount);

        // Then
        assertEquals(amount, invoice.getTotalAmount());
    }

    @Test
    @DisplayName("Should set and get due date")
    public void testDueDate() {
        // Given
        Date dueDate = new Date();

        // When
        invoice.setDueDate(dueDate);

        // Then
        assertEquals(dueDate, invoice.getDueDate());
    }

    @Test
    @DisplayName("Should set and get apartment ID")
    public void testApartmentId() {
        // When
        invoice.setApartmentId(101);

        // Then
        assertEquals(101, invoice.getApartmentId());
    }

    @Test
    @DisplayName("Should set and get owner ID")
    public void testOwnerId() {
        // When
        invoice.setOwnerId(456);

        // Then
        assertEquals(456, invoice.getOwnerId());
    }

    @Test
    @DisplayName("Should set and get status")
    public void testStatus() {
        // When
        invoice.setStatus("PAID");

        // Then
        assertEquals("PAID", invoice.getStatus());
    }

    @Test
    @DisplayName("Should add invoice detail")
    public void testAddDetail() {
        // Given
        InvoiceDetail detail1 = new InvoiceDetail(1, "Fee 1", new BigDecimal("50.00"));
        InvoiceDetail detail2 = new InvoiceDetail(2, "Fee 2", new BigDecimal("75.00"));

        // When
        invoice.addDetail(detail1);
        invoice.addDetail(detail2);

        // Then
        assertEquals(2, invoice.getDetails().size());
        assertTrue(invoice.getDetails().contains(detail1));
        assertTrue(invoice.getDetails().contains(detail2));
    }

    @Test
    @DisplayName("Should handle null detail addition gracefully")
    public void testAddNullDetail() {
        // When
        invoice.addDetail(null);

        // Then
        assertEquals(1, invoice.getDetails().size());
        assertTrue(invoice.getDetails().contains(null));
    }

    @Test
    @DisplayName("Should handle BigDecimal precision correctly")
    public void testBigDecimalPrecision() {
        // Given
        BigDecimal preciseAmount = new BigDecimal("123.456789");

        // When
        invoice.setTotalAmount(preciseAmount);

        // Then
        assertEquals(preciseAmount, invoice.getTotalAmount());
        assertEquals("123.456789", invoice.getTotalAmount().toString());
    }

    @Test
    @DisplayName("Should handle zero amount")
    public void testZeroAmount() {
        // Given
        BigDecimal zeroAmount = BigDecimal.ZERO;

        // When
        invoice.setTotalAmount(zeroAmount);

        // Then
        assertEquals(BigDecimal.ZERO, invoice.getTotalAmount());
        assertEquals(0, invoice.getTotalAmount().compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Should handle negative amount")
    public void testNegativeAmount() {
        // Given
        BigDecimal negativeAmount = new BigDecimal("-50.00");

        // When
        invoice.setTotalAmount(negativeAmount);

        // Then
        assertEquals(negativeAmount, invoice.getTotalAmount());
        assertTrue(invoice.getTotalAmount().compareTo(BigDecimal.ZERO) < 0);
    }

    @Test
    @DisplayName("Should maintain invoice details list reference")
    public void testDetailsListReference() {
        // Given
        InvoiceDetail detail = new InvoiceDetail(1, "Test Fee", new BigDecimal("100.00"));
        invoice.addDetail(detail);

        // When
        var details = invoice.getDetails();
        details.add(new InvoiceDetail(2, "Another Fee", new BigDecimal("200.00")));

        // Then
        assertEquals(2, invoice.getDetails().size());
        assertSame(details, invoice.getDetails());
    }
}