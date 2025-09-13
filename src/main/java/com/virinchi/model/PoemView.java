package com.virinchi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "poem_views")
public class PoemView {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "poem_id", nullable = false)
    private Long poemId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;
    
    // Constructors
    public PoemView() {
        this.viewedAt = LocalDateTime.now();
    }
    
    public PoemView(Long poemId, Long userId) {
        this.poemId = poemId;
        this.userId = userId;
        this.viewedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getPoemId() {
        return poemId;
    }
    
    public void setPoemId(Long poemId) {
        this.poemId = poemId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getViewedAt() {
        return viewedAt;
    }
    
    public void setViewedAt(LocalDateTime viewedAt) {
        this.viewedAt = viewedAt;
    }
}
