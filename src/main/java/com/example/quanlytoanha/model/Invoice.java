package com.example.quanlytoanha.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Invoice {
    private int invoiceId;
    private BigDecimal totalAmount;
    private Date dueDate;
    private List<InvoiceDetail> details; // Chứa danh sách các phí

    public Invoice(int invoiceId, BigDecimal totalAmount, Date dueDate) {
        this.invoiceId = invoiceId;
        this.totalAmount = totalAmount;
        this.dueDate = dueDate;
        this.details = new ArrayList<>(); // Khởi tạo danh sách
    }

    // Getters và Setters
    public int getInvoiceId() { return invoiceId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public Date getDueDate() { return dueDate; }
    public List<InvoiceDetail> getDetails() { return details; }
    public void setDetails(List<InvoiceDetail> details) { this.details = details; }

    // Phương thức tiện ích
    public void addDetail(InvoiceDetail detail) {
        this.details.add(detail);
    }
}