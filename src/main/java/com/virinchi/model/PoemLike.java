package com.virinchi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "poem_likes", uniqueConstraints = @UniqueConstraint(columnNames = {"poemId","userId"}))
public class PoemLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long poemId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PoemLike() {}

    public PoemLike(Long poemId, Long userId) {
        this.poemId = poemId;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPoemId() { return poemId; }
    public void setPoemId(Long poemId) { this.poemId = poemId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

