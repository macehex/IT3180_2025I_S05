package com.example.quanlytoanha.model;

import java.math.BigDecimal;
import java.util.Date;

// Lớp này chứa công nợ chi tiết của 1 căn hộ
public class ApartmentDebt {
    private int apartmentId;
    private String ownerName;
    private String phoneNumber;
    private int unpaidCount;      // Số hóa đơn chưa trả
    private BigDecimal totalDue;  // Tổng tiền nợ
    private Date earliestDueDate; // Ngày hạn sớm nhất

    // --- TRƯỜNG MỚI THÊM VÀO ---
    private int ownerUserId;     // ID người dùng của chủ hộ (để gửi thông báo)
    // -------------------------

    // Constructor cũ (giữ lại nếu cần)
    public ApartmentDebt(int apartmentId, String ownerName, String phoneNumber, int unpaidCount, BigDecimal totalDue, Date earliestDueDate) {
        this.apartmentId = apartmentId;
        this.ownerName = ownerName;
        this.phoneNumber = phoneNumber;
        this.unpaidCount = unpaidCount;
        this.totalDue = totalDue;
        this.earliestDueDate = earliestDueDate;
    }

    /**
     * Constructor mặc định (không tham số)
     */
    public ApartmentDebt() {
    }

    // --- Getters and Setters ---
    // (Các getter/setter cũ giữ nguyên)
    public int getApartmentId() { return apartmentId; }
    public void setApartmentId(int apartmentId) { this.apartmentId = apartmentId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public int getUnpaidCount() { return unpaidCount; }
    public void setUnpaidCount(int unpaidCount) { this.unpaidCount = unpaidCount; }
    public BigDecimal getTotalDue() { return totalDue; }
    public void setTotalDue(BigDecimal totalDue) { this.totalDue = totalDue; }
    public Date getEarliestDueDate() { return earliestDueDate; }
    public void setEarliestDueDate(Date earliestDueDate) { this.earliestDueDate = earliestDueDate; }

    // --- GETTER/SETTER CHO TRƯỜNG MỚI ---
    public int getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(int ownerUserId) { this.ownerUserId = ownerUserId; }
    // ------------------------------------

    @Override
    public String toString() {
        return "ApartmentDebt{" +
                "apartmentId=" + apartmentId +
                ", ownerName='" + ownerName + '\'' +
                ", totalDue=" + totalDue +
                ", unpaidCount=" + unpaidCount +
                ", earliestDueDate=" + earliestDueDate +
                ", ownerUserId=" + ownerUserId + // Thêm vào toString
                '}';
    }
}