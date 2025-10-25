package com.example.quanlytoanha.model;

import java.math.BigDecimal;

public class InvoiceDetail {
    private String name; // Tên phí (Phí quản lý, Tiền nước)
    private BigDecimal amount; // Số tiền

    public InvoiceDetail(String name, BigDecimal amount) {
        this.name = name;
        this.amount = amount;
    }

    // Getters và Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
