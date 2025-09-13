package com.virinchi.repository;

import com.virinchi.model.PoemView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PoemViewRepository extends JpaRepository<PoemView, Long> {
    
    // Find existing view record for a user and poem
    Optional<PoemView> findByUserIdAndPoemId(Long userId, Long poemId);
    
    // Get the latest 5 poems viewed by a user (distinct poems, ordered by most recent view)
    @Query("SELECT pv FROM PoemView pv WHERE pv.userId = :userId " +
           "AND pv.viewedAt = (SELECT MAX(pv2.viewedAt) FROM PoemView pv2 " +
           "WHERE pv2.userId = :userId AND pv2.poemId = pv.poemId) " +
           "ORDER BY pv.viewedAt DESC")
    List<PoemView> findTop5ByUserIdOrderByViewedAtDesc(@Param("userId") Long userId);
    
    // Count total views for a poem
    long countByPoemId(Long poemId);
    
    // Delete all views for a specific poem (if poem is deleted)
    void deleteByPoemId(Long poemId);
}
