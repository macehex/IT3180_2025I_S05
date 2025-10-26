package com.example.quanlytoanha.model;

import java.util.Date;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Invoice {
    private int invoiceId;
    private int apartmentId;
    private BigDecimal totalAmount;
    private Date dueDate;
    private String status;
    private List<InvoiceDetail> details;
    private int ownerId; // ID của chủ căn hộ (Lấy từ bảng apartments)

    public Invoice() {
        this.details = new ArrayList<>();
    }

    public Invoice(int invoiceId, int apartmentId, BigDecimal totalAmount, Date dueDate, String status) {
        this.invoiceId = invoiceId;
        this.apartmentId = apartmentId;
        this.totalAmount = totalAmount;
        this.dueDate = dueDate;
        this.status = status;
        this.details = new ArrayList<>();
    }

    // Getters và Setters
    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(int apartmentId) {
        this.apartmentId = apartmentId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = BigDecimal.valueOf(totalAmount);
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<InvoiceDetail> getDetails() {
        return details;
    }

    public void setDetails(List<InvoiceDetail> details) {
        this.details = details;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    // Phương thức tiện ích
    public void addDetail(InvoiceDetail detail) {
        this.details.add(detail);
    }

    @Override
    public String toString() {
        return String.format("%s - %.2f VND (Due: %s)",
            details.isEmpty() ? "Invoice #" + invoiceId : details.get(0).getName(),
            totalAmount,
            dueDate);
    }
}
