// Vị trí: src/main/java/com/example/quanlytoanha/model/Invoice.java
package com.example.quanlytoanha.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Invoice {
    private int invoiceId;
    private BigDecimal totalAmount;
    private Date dueDate;
    private List<InvoiceDetail> details;

    // --- HÀM TẠO RỖNG (ĐÃ THÊM VÀO) ---
    // (Bắt buộc phải có để các hàm trong InvoiceDAO hoạt động)
    public Invoice() {
        this.details = new ArrayList<>();
    }
    // ---------------------------------

    // Hàm tạo 3 tham số (cũ của bạn)
    public Invoice(int invoiceId, BigDecimal totalAmount, Date dueDate) {
        this.invoiceId = invoiceId;
        this.totalAmount = totalAmount;
        this.dueDate = dueDate;
        this.details = new ArrayList<>(); // Khởi tạo danh sách
    }

    // Getters và Setters
    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; } // (Thêm setter)

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; } // (Thêm setter)

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; } // (Thêm setter)

    public List<InvoiceDetail> getDetails() { return details; }
    public void setDetails(List<InvoiceDetail> details) { this.details = details; }

    // Phương thức tiện ích
    public void addDetail(InvoiceDetail detail) {
        this.details.add(detail);
    }

    // --- CÁC HÀM MỚI CẦN CHO INVOICEDAO ---
    // (Thêm các setter/getter mà InvoiceDAO cần)

    private int apartmentId;
    private int ownerId;
    private String status;

    public int getApartmentId() { return apartmentId; }
    public void setApartmentId(int apartmentId) { this.apartmentId = apartmentId; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}