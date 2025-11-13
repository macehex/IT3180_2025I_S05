// Vị trí: src/main/java/com/example/quanlytoanha/model/VehicleAccessLog.java
package com.example.quanlytoanha.model;

import java.util.Date;

public class VehicleAccessLog {

    private int logId;
    private String licensePlate;
    private String vehicleType;
    private int residentId; // 0 nếu là khách
    private String accessType; // IN hoặc OUT
    private Date accessTime;
    private int guardUserId;
    private String notes;

    // (Thêm trường Join)
    private String guardFullName;
    private String residentFullName; // Tên cư dân (nếu residentId > 0)

    // --- Getters & Setters ---

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public int getResidentId() { return residentId; }
    public void setResidentId(int residentId) { this.residentId = residentId; }

    public String getAccessType() { return accessType; }
    public void setAccessType(String accessType) { this.accessType = accessType; }

    public Date getAccessTime() { return accessTime; }
    public void setAccessTime(Date accessTime) { this.accessTime = accessTime; }

    public int getGuardUserId() { return guardUserId; }
    public void setGuardUserId(int guardUserId) { this.guardUserId = guardUserId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getGuardFullName() { return guardFullName; }
    public void setGuardFullName(String guardFullName) { this.guardFullName = guardFullName; }

    public String getResidentFullName() { return residentFullName; }
    public void setResidentFullName(String residentFullName) { this.residentFullName = residentFullName; }
}