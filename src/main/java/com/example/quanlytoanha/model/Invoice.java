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
    private int apartmentId; // ID căn hộ mà hóa đơn này thuộc về
    private int ownerId;     // ID của chủ căn hộ (Lấy từ bảng apartments)

    public Invoice() {}

    public Invoice(int invoiceId, BigDecimal totalAmount, Date dueDate) {
        this.invoiceId = invoiceId;
        this.totalAmount = totalAmount;
        this.dueDate = dueDate;
        this.details = new ArrayList<>(); // Khởi tạo danh sách
    }

    // Getters và Setters
    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public List<InvoiceDetail> getDetails() { return details; }
    public void setDetails(List<InvoiceDetail> details) { this.details = details; }

    public int getApartmentId() { return apartmentId; }
    public void setApartmentId(int apartmentId) { this.apartmentId = apartmentId; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }
    // Phương thức tiện ích
    public void addDetail(InvoiceDetail detail) {
        this.details.add(detail);
    }
}