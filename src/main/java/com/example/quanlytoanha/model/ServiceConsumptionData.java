package com.example.quanlytoanha.model;

import java.math.BigDecimal;

public class ServiceConsumptionData {
    private String serviceName;
    private BigDecimal amount;
    private double percentage;

    public ServiceConsumptionData(String serviceName, BigDecimal amount, double percentage) {
        this.serviceName = serviceName;
        this.amount = amount;
        this.percentage = percentage;
    }

    // Getters and Setters
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    @Override
    public String toString() {
        return String.format("%s: %.0f VND (%.1f%%)", serviceName, amount.doubleValue(), percentage);
    }
}