package com.example.quanlytoanha.model;

import java.math.BigDecimal;
import java.util.Map;

public class MonthlyConsumptionData {
    private int year;
    private int month;
    private Map<String, BigDecimal> serviceBreakdown;
    private BigDecimal totalAmount;

    public MonthlyConsumptionData(int year, int month, Map<String, BigDecimal> serviceBreakdown) {
        this.year = year;
        this.month = month;
        this.serviceBreakdown = serviceBreakdown;
        this.totalAmount = serviceBreakdown.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Getters and Setters
    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public Map<String, BigDecimal> getServiceBreakdown() {
        return serviceBreakdown;
    }

    public void setServiceBreakdown(Map<String, BigDecimal> serviceBreakdown) {
        this.serviceBreakdown = serviceBreakdown;
        this.totalAmount = serviceBreakdown.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getMonthYearLabel() {
        String[] months = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                          "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return months[month] + " " + year;
    }
}