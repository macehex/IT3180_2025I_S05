// Vị trí: src/main/java/com/example/quanlytoanha/model/Maintenance.java
package com.example.quanlytoanha.model;

import java.math.BigDecimal;
import java.util.Date;

public class Maintenance {
    private int maintenanceId;
    private int assetId;
    private String status;
    private Date scheduledDate;
    private Date maintenanceDate; // Ngày hoàn thành
    private String description;
    private BigDecimal cost;
    private String performedBy;
    private int createdByUserId;

    // Thêm trường join (để hiển thị tên tài sản trong bảng)
    private String assetName;

    // Constructors
    public Maintenance() {}

    // Getters and Setters
    public int getMaintenanceId() { return maintenanceId; }
    public void setMaintenanceId(int maintenanceId) { this.maintenanceId = maintenanceId; }
    public int getAssetId() { return assetId; }
    public void setAssetId(int assetId) { this.assetId = assetId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(Date scheduledDate) { this.scheduledDate = scheduledDate; }
    public Date getMaintenanceDate() { return maintenanceDate; }
    public void setMaintenanceDate(Date maintenanceDate) { this.maintenanceDate = maintenanceDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public int getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(int createdByUserId) { this.createdByUserId = createdByUserId; }
    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }
}