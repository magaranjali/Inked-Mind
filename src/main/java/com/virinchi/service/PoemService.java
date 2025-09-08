package com.virinchi.service;

import com.virinchi.model.Poem;
import com.virinchi.repository.PoemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PoemService {
    
    @Autowired
    private PoemRepository poemRepository;
    
    /**
     * Save a poem (either as draft or published)
     */
    public Poem savePoem(Poem poem) {
        return poemRepository.save(poem);
    }
    
    /**
     * Create and save a new poem with publish/draft logic
     */
    public Poem createPoem(String title, String authorName, String content, 
                          String category, String tags, boolean published, Long userId) {
        Poem poem = new Poem(title, authorName, content, published);
        poem.setCategory(category);
        poem.setTags(tags);
        poem.setUserId(userId);
        
        return savePoem(poem);
    }
    
    /**
     * Get all published poems (for public poems page)
     */
    public List<Poem> getAllPublishedPoems() {
        return poemRepository.findByPublishedTrueOrderByCreatedAtDesc();
    }
    
    /**
     * Get all poems by user (for dashboard - both published and drafts)
     */
    public List<Poem> getPoemsByUser(Long userId) {
        return poemRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Get published poems by user
     */
    public List<Poem> getPublishedPoemsByUser(Long userId) {
        return poemRepository.findByUserIdAndPublishedTrueOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Get draft poems by user
     */
    public List<Poem> getDraftPoemsByUser(Long userId) {
        return poemRepository.findByUserIdAndPublishedFalseOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Find poem by ID
     */
    public Optional<Poem> findById(Long id) {
        return poemRepository.findById(id);
    }
    
    /**
     * Publish a draft poem
     */
    public Poem publishPoem(Long poemId) {
        Optional<Poem> poemOpt = findById(poemId);
        if (poemOpt.isPresent()) {
            Poem poem = poemOpt.get();
            poem.setPublished(true);
            return savePoem(poem);
        }
        throw new RuntimeException("Poem not found with id: " + poemId);
    }
    
    /**
     * Unpublish a poem (make it draft)
     */
    public Poem unpublishPoem(Long poemId) {
        Optional<Poem> poemOpt = findById(poemId);
        if (poemOpt.isPresent()) {
            Poem poem = poemOpt.get();
            poem.setPublished(false);
            return savePoem(poem);
        }
        throw new RuntimeException("Poem not found with id: " + poemId);
    }
    
    /**
     * Delete a poem
     */
    public void deletePoem(Long poemId) {
        poemRepository.deleteById(poemId);
    }
    
    /**
     * Search published poems
     */
    public List<Poem> searchPublishedPoems(String searchTerm) {
        return poemRepository.searchPublishedPoems(searchTerm);
    }
    
    /**
     * Get poems by category
     */
    public List<Poem> getPoemsByCategory(String category) {
        return poemRepository.findByPublishedTrueAndCategoryOrderByCreatedAtDesc(category);
    }
}
