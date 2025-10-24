package com.example.quanlytoanha.model;

import java.math.BigDecimal;

/**
 * Model đại diện cho căn hộ trong hệ thống
 */
public class Apartment {
    private int apartmentId;
    private BigDecimal area;
    private int ownerId;
    private String ownerName; // Tên chủ hộ (join từ bảng users)
    
    // Constructor
    public Apartment() {}
    
    public Apartment(int apartmentId, BigDecimal area, int ownerId) {
        this.apartmentId = apartmentId;
        this.area = area;
        this.ownerId = ownerId;
    }
    
    public Apartment(int apartmentId, BigDecimal area, int ownerId, String ownerName) {
        this.apartmentId = apartmentId;
        this.area = area;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
    }
    
    // Getters and Setters
    public int getApartmentId() {
        return apartmentId;
    }
    
    public void setApartmentId(int apartmentId) {
        this.apartmentId = apartmentId;
    }
    
    public BigDecimal getArea() {
        return area;
    }
    
    public void setArea(BigDecimal area) {
        this.area = area;
    }
    
    public int getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }
    
    public String getOwnerName() {
        return ownerName;
    }
    
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    
    @Override
    public String toString() {
        return "Apartment{" +
                "apartmentId=" + apartmentId +
                ", area=" + area +
                ", ownerId=" + ownerId +
                ", ownerName='" + ownerName + '\'' +
                '}';
    }
}
