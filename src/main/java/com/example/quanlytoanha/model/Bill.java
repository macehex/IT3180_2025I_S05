package com.example.quanlytoanha.model;

import java.time.LocalDate;

public class Bill {
    private int billId;
    private double amount;
    private String description;
    private LocalDate dueDate;
    private boolean isPaid;
    private String billType;

    public Bill(int billId, double amount, String description, LocalDate dueDate, boolean isPaid, String billType) {
        this.billId = billId;
        this.amount = amount;
        this.description = description;
        this.dueDate = dueDate;
        this.isPaid = isPaid;
        this.billType = billType;
    }

    // Getters and setters
    public int getBillId() { return billId; }
    public void setBillId(int billId) { this.billId = billId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }
    public String getBillType() { return billType; }
    public void setBillType(String billType) { this.billType = billType; }
}
