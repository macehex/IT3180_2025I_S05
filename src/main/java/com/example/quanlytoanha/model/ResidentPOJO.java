// Vị trí: src/main/java/com/example/quanlytoanha/model/ResidentPOJO.java
package com.example.quanlytoanha.model;

import java.util.Date;

/**
 * POJO đại diện cho một bản ghi trong bảng 'residents'.
 * Dùng cho các mục đích CRUD cơ bản, không cần kế thừa User.
 */
public class ResidentPOJO {
    private Integer residentId;
    private Integer apartmentId;
    private Integer userId; // Có thể NULL (Integer thay vì int)
    private String fullName;
    private Date dateOfBirth;
    private String idCardNumber;
    private String relationship;
    private String status = "RESIDING"; // Mặc định: Đang cư trú
    private Date moveInDate;

    // --- Constructors ---
    public ResidentPOJO() {
        this.moveInDate = new Date(); // Mặc định ngày tạo là ngày chuyển vào
    }

    // Constructor đầy đủ (có thể thêm nếu cần)

    // --- Getters and Setters (Code đầy đủ) ---

    public Integer getResidentId() { return residentId; }
    public void setResidentId(Integer residentId) { this.residentId = residentId; }

    public Integer getApartmentId() { return apartmentId; }
    public void setApartmentId(Integer apartmentId) { this.apartmentId = apartmentId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getIdCardNumber() { return idCardNumber; }
    public void setIdCardNumber(String idCardNumber) { this.idCardNumber = idCardNumber; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getMoveInDate() { return moveInDate; }
    public void setMoveInDate(Date moveInDate) { this.moveInDate = moveInDate; }
}
