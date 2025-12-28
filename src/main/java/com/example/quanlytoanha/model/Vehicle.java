package com.example.quanlytoanha.model;

import java.time.LocalDate;

public class Vehicle {

    private int vehicleId;
    private int residentId;    // Chủ sở hữu xe
    private int apartmentId;   // Căn hộ chịu phí (Mới)
    private String licensePlate;
    private String vehicleType; // "CAR" hoặc "MOTORBIKE"
    private LocalDate registrationDate; // Ngày bắt đầu gửi (Mới)
    private boolean isActive;   // Trạng thái đang gửi hay đã hủy (Mới)

    // Trường bổ trợ hiển thị
    private String residentFullName;

    public Vehicle() {
        this.isActive = true;
        this.registrationDate = LocalDate.now();
    }

    public Vehicle(int residentId, int apartmentId, String licensePlate, String vehicleType) {
        this.residentId = residentId;
        this.apartmentId = apartmentId;
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.registrationDate = LocalDate.now();
        this.isActive = true;
    }

    // --- Getters & Setters ---

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public int getResidentId() { return residentId; }
    public void setResidentId(int residentId) { this.residentId = residentId; }

    public int getApartmentId() { return apartmentId; }
    public void setApartmentId(int apartmentId) { this.apartmentId = apartmentId; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getResidentFullName() { return residentFullName; }
    public void setResidentFullName(String residentFullName) { this.residentFullName = residentFullName; }
}