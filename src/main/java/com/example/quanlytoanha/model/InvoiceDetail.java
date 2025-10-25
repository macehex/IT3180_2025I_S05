package com.example.quanlytoanha.model;

public class InvoiceDetail {
    private int invoiceDetailId;
    private int invoiceId;
    private String name;
    private double amount;

    public InvoiceDetail(int invoiceDetailId, int invoiceId, String name, double amount) {
        this.invoiceDetailId = invoiceDetailId;
        this.invoiceId = invoiceId;
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

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
