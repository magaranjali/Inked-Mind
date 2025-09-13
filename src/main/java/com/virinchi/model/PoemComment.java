package com.virinchi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "poem_comments")
public class PoemComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long poemId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String authorName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PoemComment() {}

    public PoemComment(Long poemId, Long userId, String authorName, String content) {
        this.poemId = poemId;
        this.userId = userId;
        this.authorName = authorName;
        this.content = content;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPoemId() { return poemId; }
    public void setPoemId(Long poemId) { this.poemId = poemId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

