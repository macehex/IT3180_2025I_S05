package com.example.quanlytoanha.model;

import java.time.LocalDateTime;

public class Announcement {
    // Mapping các cột trong DB sang thuộc tính Java (camelCase)
    private int annId;          // map với ann_id
    private int authorId;       // map với author_id
    private String annTitle;    // map với ann_title
    private String content;     // map với content
    private boolean isUrgent;   // map với is_urgent
    private LocalDateTime createdAt; // map với created_at

    public Announcement() {
    }

    public Announcement(int annId, int authorId, String annTitle, String content, boolean isUrgent, LocalDateTime createdAt) {
        this.annId = annId;
        this.authorId = authorId;
        this.annTitle = annTitle;
        this.content = content;
        this.isUrgent = isUrgent;
        this.createdAt = createdAt;
    }

    // 3. Getters và Setters (Chuẩn Encapsulation của Java)

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

    public boolean isUrgent() { // Getter của boolean thường bắt đầu bằng "is"
        return isUrgent;
    }

    public void setUrgent(boolean urgent) {
        isUrgent = urgent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
