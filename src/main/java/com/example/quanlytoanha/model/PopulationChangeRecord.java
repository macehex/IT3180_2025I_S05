// Vị trí: src/main/java/com/example/quanlytoanha/model/PopulationChangeRecord.java
package com.example.quanlytoanha.model;

import java.util.Date;

public class PopulationChangeRecord {

    private String fullName;
    private String idCardNumber;
    private int apartmentId;
    private String changeType; // Giá trị sẽ là "MOVE_IN" hoặc "MOVE_OUT"
    private Date changeDate;   // Ngày chuyển đến hoặc ngày chuyển đi

    // Constructor đầy đủ tham số
    public PopulationChangeRecord(String fullName, String idCardNumber, int apartmentId, String changeType, Date changeDate) {
        this.fullName = fullName;
        this.idCardNumber = idCardNumber;
        this.apartmentId = apartmentId;
        this.changeType = changeType;
        this.changeDate = changeDate;
    }

    // --- Getters và Setters ---

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public int getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(int apartmentId) {
        this.apartmentId = apartmentId;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public Date getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }

    // Helper method để hiển thị loại biến động tiếng Việt (dùng cho TableView nếu cần)
    public String getChangeTypeVietnamese() {
        if ("MOVE_IN".equals(this.changeType)) {
            return "Chuyển đến";
        } else if ("MOVE_OUT".equals(this.changeType)) {
            return "Chuyển đi";
        }
        return this.changeType;
    }
}