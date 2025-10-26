// Vị trí: src/main/java/com/example/quanlytoanha/model/FeeType.java
package com.example.quanlytoanha.model;

import java.math.BigDecimal;

public class FeeType {
    private int feeId;
    private String feeName;
    private BigDecimal unitPrice;
    private String unit; // "m2", "xe", "can_ho" (căn hộ), "nguoi" (nhân khẩu)
    private String description;
    private boolean isActive;

    // Constructors
    public FeeType() {}

    public FeeType(String feeName, BigDecimal unitPrice, String unit, String description) {
        this.feeName = feeName;
        this.unitPrice = unitPrice;
        this.unit = unit;
        this.description = description;
        this.isActive = true;
    }

    // --- Getters và Setters ---

    public int getFeeId() { return feeId; }
    public void setFeeId(int feeId) { this.feeId = feeId; }
    public String getFeeName() { return feeName; }
    public void setFeeName(String feeName) { this.feeName = feeName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}