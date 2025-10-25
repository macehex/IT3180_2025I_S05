package com.example.quanlytoanha.model;

import java.time.LocalDateTime;

public class Transaction {
    private int transactionId;
    private double amount;
    private String description;
    private LocalDateTime timestamp;
    private String status;
    private String paymentMethod;
    private int billId;

    public Transaction(int transactionId, double amount, String description, LocalDateTime timestamp,
                      String status, String paymentMethod, int billId) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.description = description;
        this.timestamp = timestamp;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.billId = billId;
    }

    // Default constructor
    public Transaction() {
    }

    // Getters
    public int getTransactionId() { return transactionId; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
    public String getPaymentMethod() { return paymentMethod; }
    public int getBillId() { return billId; }

    // Setters
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDescription(String description) { this.description = description; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setStatus(String status) { this.status = status; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setBillId(int billId) { this.billId = billId; }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", billId=" + billId +
                '}';
    }
}
