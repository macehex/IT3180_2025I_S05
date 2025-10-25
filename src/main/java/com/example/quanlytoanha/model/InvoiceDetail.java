package com.example.quanlytoanha.model;

import java.math.BigDecimal;

public class InvoiceDetail {
    private int invoiceDetailId;
    private int invoiceId;
    private String name;
    private double amount;
    private String name; // Tên phí (Phí quản lý, Tiền nước)
    private BigDecimal amount; // Số tiền

    public InvoiceDetail(int invoiceDetailId, int invoiceId, String name, double amount) {
        this.invoiceDetailId = invoiceDetailId;
        this.invoiceId = invoiceId;
    public InvoiceDetail(String name, BigDecimal amount) {
        this.name = name;
        this.amount = amount;
    }

    public InvoiceDetail() {
    }

    // Getters and Setters
    public int getInvoiceDetailId() { return invoiceDetailId; }
    public void setInvoiceDetailId(int invoiceDetailId) { this.invoiceDetailId = invoiceDetailId; }

    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; }

    // Getters và Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
