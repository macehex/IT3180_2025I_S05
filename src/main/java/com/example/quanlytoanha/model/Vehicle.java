// Vị trí: src/main/java/com/example/quanlytoanha/model/Vehicle.java
package com.example.quanlytoanha.model;

public class Vehicle {

    private int vehicleId;
    private int residentId;
    private String licensePlate;
    private String vehicleType;

    // (Thêm trường Join để hiển thị)
    private String residentFullName;

    // --- Constructors ---
    public Vehicle() {}

    public Vehicle(int residentId, String licensePlate, String vehicleType) {
        this.residentId = residentId;
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
    }

    // --- Getters & Setters ---

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public int getResidentId() { return residentId; }
    public void setResidentId(int residentId) { this.residentId = residentId; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getResidentFullName() { return residentFullName; }
    public void setResidentFullName(String residentFullName) { this.residentFullName = residentFullName; }
}