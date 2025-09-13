package com.virinchi.repository;

import com.virinchi.model.PoemLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PoemLikeRepository extends JpaRepository<PoemLike, Long> {
    long countByPoemId(Long poemId);
    Optional<PoemLike> findByPoemIdAndUserId(Long poemId, Long userId);
    void deleteByPoemIdAndUserId(Long poemId, Long userId);
    long countByPoemIdIn(List<Long> poemIds);
}

