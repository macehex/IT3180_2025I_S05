// Vị trí: src/main/java/com/example/quanlytoanha/model/Asset.java
package com.example.quanlytoanha.model;

import java.math.BigDecimal;
import java.util.Date;

public class Asset {
    private int assetId;
    private String assetType;
    private String description;
    private String location;
    private String status;
    private Date purchaseDate;
    private BigDecimal initialCost;

    // Constructors
    public Asset() {}

    public Asset(int assetId, String assetType, String description, String location, String status, Date purchaseDate, BigDecimal initialCost) {
        this.assetId = assetId;
        this.assetType = assetType;
        this.description = description;
        this.location = location;
        this.status = status;
        this.purchaseDate = purchaseDate;
        this.initialCost = initialCost;
    }

    // Getters và Setters
    public int getAssetId() { return assetId; }
    public void setAssetId(int assetId) { this.assetId = assetId; }

    public String getAssetType() { return assetType; }
    public void setAssetType(String assetType) { this.assetType = assetType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(Date purchaseDate) { this.purchaseDate = purchaseDate; }

    public BigDecimal getInitialCost() { return initialCost; }
    public void setInitialCost(BigDecimal initialCost) { this.initialCost = initialCost; }
}
