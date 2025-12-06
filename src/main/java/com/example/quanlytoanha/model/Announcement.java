package com.example.quanlytoanha.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Model cho thông báo chung (Announcement)
 */
public class Announcement {
    private int annId;
    private int authorId;
    private String annTitle;
    private String content;
    private boolean isUrgent;
    private Timestamp createdAt;

    // Constructors
    public Announcement() {}

    public Announcement(int authorId, String annTitle, String content, boolean isUrgent) {
        this.authorId = authorId;
        this.annTitle = annTitle;
        this.content = content;
        this.isUrgent = isUrgent;
    }

    // Getters and Setters
    public int getAnnId() {
        return annId;
    }

    public void setAnnId(int annId) {
        this.annId = annId;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getAnnTitle() {
        return annTitle;
    }

    public void setAnnTitle(String annTitle) {
        this.annTitle = annTitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isUrgent() {
        return isUrgent;
    }

    public void setUrgent(boolean urgent) {
        isUrgent = urgent;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}

