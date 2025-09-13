package com.virinchi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.virinchi.model.ContestEntry;
import com.virinchi.model.ContestEntry.EntryStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContestEntryRepository extends JpaRepository<ContestEntry, Long> {
    
    // Find all entries for a specific contest
    List<ContestEntry> findByContestId(String contestId);
    
    // Find all entries by a specific user
    List<ContestEntry> findByUserId(Long userId);
    
    // Find entries by user and contest
    List<ContestEntry> findByUserIdAndContestId(Long userId, String contestId);
    
    // Find entries by status
    List<ContestEntry> findByStatus(EntryStatus status);
    
    // Find entries by user and status
    List<ContestEntry> findByUserIdAndStatus(Long userId, EntryStatus status);
    
    // Find a specific entry by user and contest (useful for checking if user already has an entry)
    Optional<ContestEntry> findByUserIdAndContestIdAndStatus(Long userId, String contestId, EntryStatus status);
    
    // Count entries for a contest
    long countByContestId(String contestId);
    
    // Count submitted entries for a contest
    long countByContestIdAndStatus(String contestId, EntryStatus status);
    
    // Find entries by user ordered by creation date (most recent first)
    List<ContestEntry> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Find all entries ordered by creation date (most recent first) - for admin
    List<ContestEntry> findAllByOrderByCreatedAtDesc();
}
