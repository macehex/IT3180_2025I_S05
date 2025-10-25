package com.example.quanlytoanha.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Invoice {
    private int invoiceId;
    private int apartmentId;
    private double totalAmount;
    private LocalDate dueDate;
    private String status;
    private List<InvoiceDetail> details;

    public Invoice(int invoiceId, int apartmentId, double totalAmount, LocalDate dueDate, String status) {
        this.invoiceId = invoiceId;
        this.apartmentId = apartmentId;
        this.totalAmount = totalAmount;
        this.dueDate = dueDate;
        this.status = status;
        this.details = new ArrayList<>();
    }

    public Invoice() {
        this.details = new ArrayList<>();
    }

    // Getters and Setters
    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; }

    public int getApartmentId() { return apartmentId; }
    public void setApartmentId(int apartmentId) { this.apartmentId = apartmentId; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<InvoiceDetail> getDetails() { return details; }
    public void setDetails(List<InvoiceDetail> details) { this.details = details; }

    @Override
    public String toString() {
        return String.format("%s - %.2f VND (Due: %s)",
            details.isEmpty() ? "Invoice #" + invoiceId : details.get(0).getName(),
            totalAmount,
            dueDate);
    }
}
