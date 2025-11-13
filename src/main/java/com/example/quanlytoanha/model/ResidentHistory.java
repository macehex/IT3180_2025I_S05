// Vị trí: src/main/java/com/example/quanlytoanha/model/ResidentHistory.java
package com.example.quanlytoanha.model;

import java.util.Date;

public class ResidentHistory {
    private int historyId;
    private int residentId;
    private int changedByUserId;
    private Date changedAt;
    private String oldData; // Lưu trữ JSON dưới dạng String
    private String newData; // Lưu trữ JSON dưới dạng String

    // Thêm các trường join để hiển thị
    private String changedByUserFullName; // Tên BQT đã thay đổi
    private String residentFullName;      // Tên Cư dân

    // Getters và Setters
    public int getHistoryId() { return historyId; }
    public void setHistoryId(int historyId) { this.historyId = historyId; }
    public int getResidentId() { return residentId; }
    public void setResidentId(int residentId) { this.residentId = residentId; }
    public int getChangedByUserId() { return changedByUserId; }
    public void setChangedByUserId(int changedByUserId) { this.changedByUserId = changedByUserId; }
    public Date getChangedAt() { return changedAt; }
    public void setChangedAt(Date changedAt) { this.changedAt = changedAt; }
    public String getOldData() { return oldData; }
    public void setOldData(String oldData) { this.oldData = oldData; }
    public String getNewData() { return newData; }
    public void setNewData(String newData) { this.newData = newData; }
    public String getChangedByUserFullName() { return changedByUserFullName; }
    public void setChangedByUserFullName(String changedByUserFullName) { this.changedByUserFullName = changedByUserFullName; }
    public String getResidentFullName() { return residentFullName; }
    public void setResidentFullName(String residentFullName) { this.residentFullName = residentFullName; }
}