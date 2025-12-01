// Vị trí: src/main/java/com/example/quanlytoanha/model/AssetReport.java
package com.example.quanlytoanha.model;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Model chứa dữ liệu báo cáo tài sản
 */
public class AssetReport {
    
    // Báo cáo theo tình trạng
    private Map<String, Integer> statusCounts;
    
    // Báo cáo theo vị trí
    private Map<String, Integer> locationCounts;
    
    // Báo cáo chi phí bảo trì theo tài sản
    private Map<Integer, BigDecimal> maintenanceCostByAsset;
    
    // Báo cáo chi phí bảo trì theo vị trí
    private Map<String, BigDecimal> maintenanceCostByLocation;
    
    // Báo cáo chi phí bảo trì theo tình trạng
    private Map<String, BigDecimal> maintenanceCostByStatus;
    
    // Tổng chi phí bảo trì
    private BigDecimal totalMaintenanceCost;
    
    // Tổng số tài sản
    private int totalAssets;
    
    // Tổng giá trị ban đầu
    private BigDecimal totalInitialCost;

    // Constructors
    public AssetReport() {}

    // Getters and Setters
    public Map<String, Integer> getStatusCounts() {
        return statusCounts;
    }

    public void setStatusCounts(Map<String, Integer> statusCounts) {
        this.statusCounts = statusCounts;
    }

    public Map<String, Integer> getLocationCounts() {
        return locationCounts;
    }

    public void setLocationCounts(Map<String, Integer> locationCounts) {
        this.locationCounts = locationCounts;
    }

    public Map<Integer, BigDecimal> getMaintenanceCostByAsset() {
        return maintenanceCostByAsset;
    }

    public void setMaintenanceCostByAsset(Map<Integer, BigDecimal> maintenanceCostByAsset) {
        this.maintenanceCostByAsset = maintenanceCostByAsset;
    }

    public Map<String, BigDecimal> getMaintenanceCostByLocation() {
        return maintenanceCostByLocation;
    }

    public void setMaintenanceCostByLocation(Map<String, BigDecimal> maintenanceCostByLocation) {
        this.maintenanceCostByLocation = maintenanceCostByLocation;
    }

    public Map<String, BigDecimal> getMaintenanceCostByStatus() {
        return maintenanceCostByStatus;
    }

    public void setMaintenanceCostByStatus(Map<String, BigDecimal> maintenanceCostByStatus) {
        this.maintenanceCostByStatus = maintenanceCostByStatus;
    }

    public BigDecimal getTotalMaintenanceCost() {
        return totalMaintenanceCost;
    }

    public void setTotalMaintenanceCost(BigDecimal totalMaintenanceCost) {
        this.totalMaintenanceCost = totalMaintenanceCost;
    }

    public int getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(int totalAssets) {
        this.totalAssets = totalAssets;
    }

    public BigDecimal getTotalInitialCost() {
        return totalInitialCost;
    }

    public void setTotalInitialCost(BigDecimal totalInitialCost) {
        this.totalInitialCost = totalInitialCost;
    }
}

