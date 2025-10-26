package com.example.quanlytoanha.model;

import java.math.BigDecimal;

public class FeeType {
    private int id;
    private String feeName;
    private BigDecimal unitPrice;
    private String unit;
    private String description;
    private boolean isActive;

    // Constructor rỗng (cần cho JavaFX)
    public FeeType() {
    }

    // Constructor đầy đủ (dùng khi tạo đối tượng từ CSDL)
    public FeeType(int id, String feeName, BigDecimal unitPrice, String unit, String description, boolean isActive) {
        this.id = id;
        this.feeName = feeName;
        this.unitPrice = unitPrice;
        this.unit = unit;
        this.description = description;
        this.isActive = isActive;
    }

    // --- Getters and Setters cho tất cả các trường ---
    // (Bắt buộc phải có đủ để PropertyValueFactory của JavaFX hoạt động)

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

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
