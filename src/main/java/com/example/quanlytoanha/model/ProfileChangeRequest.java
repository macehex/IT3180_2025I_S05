package com.example.quanlytoanha.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Model for profile change requests submitted by residents
 * Admin needs to approve these requests before changes take effect
 */
public class ProfileChangeRequest {
    
    private int requestId;
    private int userId;  // The user requesting the change
    private String requestType;  // Type: "PROFILE_CHANGE"
    private String status;  // PENDING, APPROVED, REJECTED
    private Timestamp createdAt;
    private Timestamp processedAt;
    private int processedBy;  // Admin user ID who processed the request
    private String adminComment;  // Admin's comment when approving/rejecting
    
    // Current values
    private String currentUsername;
    private String currentPhoneNumber;
    private String currentEmail;
    private String currentFullName;
    private String currentRelationship;
    private Date currentDateOfBirth;
    private String currentIdCardNumber;
    
    // Requested new values
    private String newUsername;
    private String newPhoneNumber;
    private String newEmail;
    private String newFullName;
    private String newRelationship;
    private Date newDateOfBirth;
    private String newIdCardNumber;
    
    // Additional fields for display
    private String requesterFullName;
    private int apartmentId;
    
    public ProfileChangeRequest() {
    }
    
    public ProfileChangeRequest(int userId, String currentUsername, String currentPhoneNumber,
                               String currentEmail, String currentFullName, String currentRelationship,
                               Date currentDateOfBirth, String currentIdCardNumber,
                               String newUsername, String newPhoneNumber, String newEmail,
                               String newFullName, String newRelationship, Date newDateOfBirth,
                               String newIdCardNumber) {
        this.userId = userId;
        this.requestType = "PROFILE_CHANGE";
        this.status = "PENDING";
        this.currentUsername = currentUsername;
        this.currentPhoneNumber = currentPhoneNumber;
        this.currentEmail = currentEmail;
        this.currentFullName = currentFullName;
        this.currentRelationship = currentRelationship;
        this.currentDateOfBirth = currentDateOfBirth;
        this.currentIdCardNumber = currentIdCardNumber;
        this.newUsername = newUsername;
        this.newPhoneNumber = newPhoneNumber;
        this.newEmail = newEmail;
        this.newFullName = newFullName;
        this.newRelationship = newRelationship;
        this.newDateOfBirth = newDateOfBirth;
        this.newIdCardNumber = newIdCardNumber;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    // Getters and Setters
    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Timestamp processedAt) {
        this.processedAt = processedAt;
    }

    public int getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(int processedBy) {
        this.processedBy = processedBy;
    }

    public String getAdminComment() {
        return adminComment;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public void setCurrentUsername(String currentUsername) {
        this.currentUsername = currentUsername;
    }

    public String getCurrentPhoneNumber() {
        return currentPhoneNumber;
    }

    public void setCurrentPhoneNumber(String currentPhoneNumber) {
        this.currentPhoneNumber = currentPhoneNumber;
    }

    public String getCurrentEmail() {
        return currentEmail;
    }

    public void setCurrentEmail(String currentEmail) {
        this.currentEmail = currentEmail;
    }

    public String getCurrentFullName() {
        return currentFullName;
    }

    public void setCurrentFullName(String currentFullName) {
        this.currentFullName = currentFullName;
    }

    public String getCurrentRelationship() {
        return currentRelationship;
    }

    public void setCurrentRelationship(String currentRelationship) {
        this.currentRelationship = currentRelationship;
    }

    public Date getCurrentDateOfBirth() {
        return currentDateOfBirth;
    }

    public void setCurrentDateOfBirth(Date currentDateOfBirth) {
        this.currentDateOfBirth = currentDateOfBirth;
    }

    public String getCurrentIdCardNumber() {
        return currentIdCardNumber;
    }

    public void setCurrentIdCardNumber(String currentIdCardNumber) {
        this.currentIdCardNumber = currentIdCardNumber;
    }

    public String getNewUsername() {
        return newUsername;
    }

    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }

    public String getNewPhoneNumber() {
        return newPhoneNumber;
    }

    public void setNewPhoneNumber(String newPhoneNumber) {
        this.newPhoneNumber = newPhoneNumber;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public String getNewFullName() {
        return newFullName;
    }

    public void setNewFullName(String newFullName) {
        this.newFullName = newFullName;
    }

    public String getNewRelationship() {
        return newRelationship;
    }

    public void setNewRelationship(String newRelationship) {
        this.newRelationship = newRelationship;
    }

    public Date getNewDateOfBirth() {
        return newDateOfBirth;
    }

    public void setNewDateOfBirth(Date newDateOfBirth) {
        this.newDateOfBirth = newDateOfBirth;
    }

    public String getNewIdCardNumber() {
        return newIdCardNumber;
    }

    public void setNewIdCardNumber(String newIdCardNumber) {
        this.newIdCardNumber = newIdCardNumber;
    }

    public String getRequesterFullName() {
        return requesterFullName;
    }

    public void setRequesterFullName(String requesterFullName) {
        this.requesterFullName = requesterFullName;
    }

    public int getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(int apartmentId) {
        this.apartmentId = apartmentId;
    }
}