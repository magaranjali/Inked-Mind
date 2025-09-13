package com.virinchi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.virinchi.model.Poem;
import com.virinchi.model.PoemView;
import com.virinchi.model.UserClass;
import com.virinchi.repository.PoemRepository;
import com.virinchi.repository.PoemLikeRepository;
import com.virinchi.repository.PoemCommentRepository;
import com.virinchi.repository.PoemViewRepository;
import com.virinchi.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
public class PoemsController {
	
    @Autowired
    private UserRepository uRepo;
    
    @Autowired
    private PoemRepository poemRepo;
    
    @Autowired
    private PoemLikeRepository likeRepo;
    
    @Autowired
    private PoemCommentRepository commentRepo;
    
    @Autowired
    private PoemViewRepository poemViewRepo;
	
    // Helper method to get like and comment counts for a poem
    private Map<String, Object> getPoemCounts(Long poemId) {
        Map<String, Object> counts = new HashMap<>();
        counts.put("likes", likeRepo.countByPoemId(poemId));
        counts.put("comments", commentRepo.countByPoemId(poemId));
        return counts;
    }
    
    // Helper method to check if user has liked a poem
    private boolean hasUserLikedPoem(Long poemId, Long userId) {
        if (userId == null) return false;
        return likeRepo.findByPoemIdAndUserId(poemId, userId).isPresent();
    }
    
    // Helper method to record poem view
    private void recordPoemView(Long poemId, Long userId) {
        try {
            // Check if user has already viewed this poem
            var existingView = poemViewRepo.findByUserIdAndPoemId(userId, poemId);
            if (existingView.isPresent()) {
                // Update existing view with new timestamp
                PoemView poemView = existingView.get();
                poemView.setViewedAt(java.time.LocalDateTime.now());
                poemViewRepo.save(poemView);
            } else {
                // Create new view record
                PoemView poemView = new PoemView(poemId, userId);
                poemViewRepo.save(poemView);
            }
        } catch (Exception e) {
            // Log error but don't break the flow
            System.err.println("Error recording poem view: " + e.getMessage());
        }
    }
	
    @GetMapping("/poems")
    public String poemsPage(Model model, HttpSession session) {
        // Check if user is logged in
        String activeUser = (String) session.getAttribute("activeUser");
        if (activeUser == null) {
            return "redirect:/login";
        }
        
        // Get all published poems for public display
        List<Poem> publishedPoems = poemRepo.findByPublishedTrueOrderByCreatedAtDesc();
        model.addAttribute("poems", publishedPoems);
        
        // Add like and comment counts for each poem
        Map<Long, Map<String, Object>> poemCounts = new HashMap<>();
        for (Poem poem : publishedPoems) {
            poemCounts.put(poem.getId(), getPoemCounts(poem.getId()));
        }
        model.addAttribute("poemCounts", poemCounts);
        
        // Add user info if logged in
        UserClass user = null;
        if (activeUser != null) {
            user = uRepo.findTopByEmailOrderByIdDesc(activeUser);
            if (user != null) {
                model.addAttribute("currentUser", user);
                model.addAttribute("activeUser", activeUser);
                
                // Add liked status for each poem for the current user
                Map<Long, Boolean> userLikes = new HashMap<>();
                for (Poem poem : publishedPoems) {
                    userLikes.put(poem.getId(), hasUserLikedPoem(poem.getId(), Long.valueOf(user.getId())));
                }
                model.addAttribute("userLikes", userLikes);
            }
        }
        
        return "poems"; 
    }
    
    @GetMapping("/poems/create")
    public String createPoemPage(HttpSession session, Model model) {
        String activeUser = (String) session.getAttribute("activeUser");
        
        if (activeUser != null) {
            UserClass user = uRepo.findTopByEmailOrderByIdDesc(activeUser);
            if (user != null) {
                model.addAttribute("user", user);
                model.addAttribute("activeUser", activeUser);
            }
        }
        
        return "create-poem";
    }

    @GetMapping("/poem/{id}")
    public String viewPoem(@PathVariable("id") Long id, HttpSession session, Model model, RedirectAttributes ra) {
        Poem poem = poemRepo.findById(id).orElse(null);
        if (poem == null) {
            ra.addFlashAttribute("errorMessage", "Poem not found.");
            return "redirect:/poems";
        }

        // Access control: published poems are public; drafts only for owner
        if (!poem.isPublished()) {
            String email = (String) session.getAttribute("activeUser");
            if (email == null) {
                ra.addFlashAttribute("errorMessage", "Please log in to view this draft.");
                return "redirect:/login";
            }
            UserClass user = uRepo.findTopByEmailOrderByIdDesc(email);
            if (user == null || poem.getUserId() == null || !poem.getUserId().equals(Long.valueOf(user.getId()))) {
                ra.addFlashAttribute("errorMessage", "You do not have permission to view this poem.");
                return "redirect:/poems";
            }
        }

        model.addAttribute("poem", poem);
        
        // Add like and comment counts for this poem
        Map<String, Object> counts = getPoemCounts(id);
        model.addAttribute("poemCounts", counts);
        
        // Add user like status if logged in and record poem view
        String activeUser = (String) session.getAttribute("activeUser");
        if (activeUser != null) {
            UserClass user = uRepo.findTopByEmailOrderByIdDesc(activeUser);
            if (user != null) {
                boolean hasLiked = hasUserLikedPoem(id, Long.valueOf(user.getId()));
                model.addAttribute("hasUserLiked", hasLiked);
                model.addAttribute("currentUser", user);
                
                // Record poem view
                recordPoemView(id, Long.valueOf(user.getId()));
            }
        }
        
        return "poem";
    }
    
    @PostMapping("/poem/save")
    public String savePoem(
            @RequestParam("title") String title,
            @RequestParam("authorName") String authorName,
            @RequestParam("content") String content,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam("action") String action,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        String activeUser = (String) session.getAttribute("activeUser");
        if (activeUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "You must be logged in to create poems.");
            return "redirect:/login";
        }
        
        try {
            UserClass user = uRepo.findTopByEmailOrderByIdDesc(activeUser);
            if (user == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
                return "redirect:/login";
            }
            
            // Create new poem
            Poem poem = new Poem();
            poem.setTitle(title);
            poem.setAuthorName(authorName);
            poem.setContent(content);
            poem.setCategory(category);
            poem.setTags(tags);
            poem.setUserId(Long.valueOf(user.getId()));
            
            // Set published status based on action
            boolean isPublished = "publish".equals(action);
            poem.setPublished(isPublished);
            
            // Save poem
            poemRepo.save(poem);
            
            // Set success message based on action
            if (isPublished) {
                redirectAttributes.addFlashAttribute("successMessage", "Poem published successfully!");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Poem saved as draft successfully!");
            }
            
            return "redirect:/dashboard";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving poem: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

}
