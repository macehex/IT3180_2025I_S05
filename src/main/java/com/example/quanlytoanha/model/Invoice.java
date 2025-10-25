package com.example.quanlytoanha.model;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Invoice {
    private int invoiceId;
    private int apartmentId;
    private double totalAmount;
    private LocalDate dueDate;
    private String status;
    private List<InvoiceDetail> details;
    private BigDecimal totalAmount;
    private Date dueDate;
    private List<InvoiceDetail> details; // Chứa danh sách các phí
    private int apartmentId; // ID căn hộ mà hóa đơn này thuộc về
    private int ownerId;     // ID của chủ căn hộ (Lấy từ bảng apartments)

    public Invoice(int invoiceId, int apartmentId, double totalAmount, LocalDate dueDate, String status) {
    public Invoice() {}

    public Invoice(int invoiceId, BigDecimal totalAmount, Date dueDate) {
        this.invoiceId = invoiceId;
        this.apartmentId = apartmentId;
        this.totalAmount = totalAmount;
        this.dueDate = dueDate;
        this.status = status;
        this.details = new ArrayList<>();
    }

    public Invoice() {
        this.details = new ArrayList<>();
        this.details = new ArrayList<>(); // Khởi tạo danh sách
    }

    // Getters and Setters
    // Getters và Setters
    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; }

    public int getApartmentId() { return apartmentId; }
    public void setApartmentId(int apartmentId) { this.apartmentId = apartmentId; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public List<InvoiceDetail> getDetails() { return details; }
    public void setDetails(List<InvoiceDetail> details) { this.details = details; }

    @Override
    public String toString() {
        return String.format("%s - %.2f VND (Due: %s)",
            details.isEmpty() ? "Invoice #" + invoiceId : details.get(0).getName(),
            totalAmount,
            dueDate);
    public int getApartmentId() { return apartmentId; }
    public void setApartmentId(int apartmentId) { this.apartmentId = apartmentId; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }
    // Phương thức tiện ích
    public void addDetail(InvoiceDetail detail) {
        this.details.add(detail);
    }
}
