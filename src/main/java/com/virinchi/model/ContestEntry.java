package com.virinchi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contest_entries")
public class ContestEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String contestId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String authorName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryStatus status;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum ContestType {
        BASIC, PREMIUM
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContestType contestType = ContestType.BASIC;
    
    public enum EntryStatus {
        DRAFT, SUBMITTED
    }
    
    // Default constructor
    public ContestEntry() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Constructor with required fields
    public ContestEntry(String contestId, String title, String content, Long userId, String authorName, EntryStatus status) {
        this();
        this.contestId = contestId;
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.authorName = authorName;
        this.status = status;
    }

    // Constructor with contest type
    public ContestEntry(String contestId, String title, String content, Long userId, String authorName, EntryStatus status, ContestType type) {
        this(contestId, title, content, userId, authorName, status);
        this.contestType = type == null ? ContestType.BASIC : type;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getContestId() {
        return contestId;
    }
    
    public void setContestId(String contestId) {
        this.contestId = contestId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getAuthorName() {
        return authorName;
    }
    
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    
    public EntryStatus getStatus() {
        return status;
    }
    
    public void setStatus(EntryStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ContestType getContestType() {
        return contestType;
    }

    public void setContestType(ContestType contestType) {
        this.contestType = contestType == null ? ContestType.BASIC : contestType;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
