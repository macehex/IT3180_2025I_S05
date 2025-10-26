// Vị trí: src/main/java/com/example/quanlytoanha/model/FeeType.java
package com.example.quanlytoanha.model;

import java.math.BigDecimal;

public class FeeType {
    private int feeId; // Đã đổi tên từ 'id' -> 'feeId' để khớp với 'fee_id'
    private String feeName;
    private BigDecimal unitPrice;
    private String unit;
    private String description;
    private boolean isActive;

    // --- CÁC TRƯỜNG MỚI ĐƯỢC THÊM ---
    private boolean isDefault; // TRUE = mặc định, FALSE = tùy chọn
    private String pricingModel; // 'FIXED' hoặc 'PER_SQM'

    // Constructor rỗng
    public FeeType() {
    }

    // Constructor đầy đủ (dùng khi tạo đối tượng từ CSDL)
    // Đã cập nhật constructor này để bao gồm các trường mới
    public FeeType(int feeId, String feeName, BigDecimal unitPrice, String unit, String description, boolean isActive, boolean isDefault, String pricingModel) {
        this.feeId = feeId;
        this.feeName = feeName;
        this.unitPrice = unitPrice;
        this.unit = unit;
        this.description = description;
        this.isActive = isActive;
        this.isDefault = isDefault;
        this.pricingModel = pricingModel;
    }

    // --- Getters and Setters cho tất cả các trường ---

    public int getFeeId() { return feeId; } // Đã đổi tên
    public void setFeeId(int feeId) { this.feeId = feeId; } // Đã đổi tên

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

    // --- GETTER/SETTER CHO CÁC TRƯỜNG MỚI ---
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    public String getPricingModel() { return pricingModel; }
    public void setPricingModel(String pricingModel) { this.pricingModel = pricingModel; }
}
