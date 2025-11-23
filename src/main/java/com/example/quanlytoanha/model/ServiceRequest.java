package com.example.quanlytoanha.model;

import java.sql.Date;

public class ServiceRequest {

    private int requestId;
    private int reqUserId;       // Tương ứng với req_user_id (người gửi yêu cầu)
    private String reqType;      // Tương ứng với req_type (loại yêu cầu, VD: SU_CO, PHAN_ANH)
    private String reqTitle;
    private String description;
    private String status;
    private Date createdAt;      // Tương ứng với created_at
    private Date completedAt;    // Tương ứng với completed_at
    private Integer assetId;     // Tương ứng với asset_id (ID của tài sản liên quan)
    private String imageUrl;

    // Constructor rỗng (cần thiết cho một số thư viện và framework)
    public ServiceRequest() {
    }

    // Constructor đầy đủ (hữu ích khi bạn đọc dữ liệu từ ResultSet)
    public ServiceRequest(int requestId, int reqUserId, String reqType, String reqTitle,
                          String description, String status, Date createdAt,
                          Date completedAt, Integer assetId) {
        this.requestId = requestId;
        this.reqUserId = reqUserId;
        this.reqType = reqType;
        this.reqTitle = reqTitle;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.assetId = assetId;
        this.imageUrl = imageUrl;
    }

    // --- Getters và Setters cho tất cả các trường ---

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getReqUserId() {
        return reqUserId;
    }

    public void setReqUserId(int reqUserId) {
        this.reqUserId = reqUserId;
    }

    public String getReqType() {
        return reqType;
    }

    public void setReqType(String reqType) {
        this.reqType = reqType;
    }

    public String getReqTitle() {
        return reqTitle;
    }

    public void setReqTitle(String reqTitle) {
        this.reqTitle = reqTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getAssetId() {
        return assetId;
    }

    public void setAssetId(Integer assetId) {
        this.assetId = assetId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}