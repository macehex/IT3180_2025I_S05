// Vị trí: src/main/java/com/example/quanlytoanha/model/InvoiceDetail.java
package com.example.quanlytoanha.model;

import java.math.BigDecimal;

public class InvoiceDetail {
    private int feeId; // <-- TRƯỜNG MỚI ĐƯỢC THÊM
    private String name; // Tên phí (Phí quản lý, Tiền nước)
    private BigDecimal amount; // Số tiền

    // Constructor đã được cập nhật (để khớp với FinancialDAO)
    public InvoiceDetail(int feeId, String name, BigDecimal amount) {
        this.feeId = feeId;
        this.name = name;
        this.amount = amount;
    }

    // Getters và Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    // --- GETTER/SETTER CHO TRƯỜNG MỚI ---
    public int getFeeId() { return feeId; }
    public void setFeeId(int feeId) { this.feeId = feeId; }
}