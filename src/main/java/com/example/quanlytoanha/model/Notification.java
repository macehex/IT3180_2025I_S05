package com.example.quanlytoanha.model;

import java.sql.Timestamp; // Sử dụng Timestamp vì cột là TIMESTAMPTZ

public class Notification {
    private int notificationId;
    private int userId; // Người nhận
    private String title;
    private String message;
    private Timestamp createdAt;
    private boolean isRead;
    private Integer relatedInvoiceId; // Dùng Integer để cho phép NULL

    // --- Constructors ---
    public Notification() {}

    public Notification(int userId, String title, String message, Integer relatedInvoiceId) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.relatedInvoiceId = relatedInvoiceId;
        this.isRead = false; // Mặc định là chưa đọc
        // createdAt sẽ do DB tự gán
    }

    // --- Getters and Setters ---
    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public Integer getRelatedInvoiceId() { return relatedInvoiceId; }
    public void setRelatedInvoiceId(Integer relatedInvoiceId) { this.relatedInvoiceId = relatedInvoiceId; }
}