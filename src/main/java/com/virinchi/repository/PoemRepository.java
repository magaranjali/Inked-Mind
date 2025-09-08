package com.virinchi.repository;

import com.virinchi.model.Poem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PoemRepository extends JpaRepository<Poem, Long> {
    
    // Find all published poems (for public poems page)
    List<Poem> findByPublishedTrueOrderByCreatedAtDesc();
    
    // Find all poems by user ID (for dashboard - both published and drafts)
    List<Poem> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Find published poems by user ID
    List<Poem> findByUserIdAndPublishedTrueOrderByCreatedAtDesc(Long userId);
    
    // Find draft poems by user ID
    List<Poem> findByUserIdAndPublishedFalseOrderByCreatedAtDesc(Long userId);
    
    // Find poems by category (published only)
    List<Poem> findByPublishedTrueAndCategoryOrderByCreatedAtDesc(String category);
    
    // Search poems by title or content (published only)
    @Query("SELECT p FROM Poem p WHERE p.published = true AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) ORDER BY p.createdAt DESC")
    List<Poem> searchPublishedPoems(@Param("searchTerm") String searchTerm);
}
