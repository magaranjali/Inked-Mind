package com.virinchi.repository;

import com.virinchi.model.PoemComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PoemCommentRepository extends JpaRepository<PoemComment, Long> {
    long countByPoemId(Long poemId);
    List<PoemComment> findTop20ByPoemIdOrderByCreatedAtDesc(Long poemId);
    long countByPoemIdIn(List<Long> poemIds);
}

