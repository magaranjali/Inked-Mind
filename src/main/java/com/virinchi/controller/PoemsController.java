package com.virinchi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.virinchi.model.Poem;
import com.virinchi.model.UserClass;
import com.virinchi.repository.PoemRepository;
import com.virinchi.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
public class PoemsController {
	
    @Autowired
    private UserRepository uRepo;
    
    @Autowired
    private PoemRepository poemRepo;
	
    @GetMapping("/poems")
    public String poemsPage(Model model, HttpSession session) {
        // Get all published poems for public display
        List<Poem> publishedPoems = poemRepo.findByPublishedTrueOrderByCreatedAtDesc();
        model.addAttribute("poems", publishedPoems);
        
        // Add user info if logged in
        String activeUser = (String) session.getAttribute("activeUser");
        if (activeUser != null) {
            UserClass user = uRepo.findTopByEmailOrderByIdDesc(activeUser);
            if (user != null) {
                model.addAttribute("currentUser", user);
                model.addAttribute("activeUser", activeUser);
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
