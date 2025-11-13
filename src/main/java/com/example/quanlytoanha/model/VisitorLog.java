// Vị trí: src/main/java/com/example/quanlytoanha/model/VisitorLog.java
package com.example.quanlytoanha.model;

import java.util.Date;

public class VisitorLog {

    private int logId;
    private String visitorName;
    private String idCardNumber;
    private String contactPhone;
    private String reason;

    // --- SỬA LỖI: Thay đổi int sang Integer để cho phép NULL ---
    private Integer apartmentId;

    private Date checkInTime;
    private Date checkOutTime;
    private int guardUserId;

    private String guardFullName;

    // --- Getters & Setters (Đã cập nhật) ---

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public String getVisitorName() { return visitorName; }
    public void setVisitorName(String visitorName) { this.visitorName = visitorName; }

    public String getIdCardNumber() { return idCardNumber; }
    public void setIdCardNumber(String idCardNumber) { this.idCardNumber = idCardNumber; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    // --- SỬA LỖI: Cập nhật Getters/Setters cho Integer ---
    public Integer getApartmentId() { return apartmentId; }
    public void setApartmentId(Integer apartmentId) { this.apartmentId = apartmentId; }

    public Date getCheckInTime() { return checkInTime; }
    public void setCheckInTime(Date checkInTime) { this.checkInTime = checkInTime; }

    public Date getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(Date checkOutTime) { this.checkOutTime = checkOutTime; }

    public int getGuardUserId() { return guardUserId; }
    public void setGuardUserId(int guardUserId) { this.guardUserId = guardUserId; }

    public String getGuardFullName() { return guardFullName; }
    public void setGuardFullName(String guardFullName) { this.guardFullName = guardFullName; }
}