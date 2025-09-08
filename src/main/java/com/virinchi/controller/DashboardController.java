package com.virinchi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.virinchi.model.Poem;
import com.virinchi.model.UserClass;
import com.virinchi.repository.PoemRepository;
import com.virinchi.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private UserRepository uRepo;
    
    @Autowired
    private PoemRepository poemRepo;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String activeUser = (String) session.getAttribute("activeUser");

        if (activeUser != null) {
            session.setAttribute("activeUser", activeUser);
            model.addAttribute("activeUser", activeUser);

            UserClass user = uRepo.findTopByEmailOrderByIdDesc(activeUser);
            if (user != null) {
                model.addAttribute("user", user);
                model.addAttribute("currentUser", user);
                
                // Get user's poems (both published and drafts)
                List<Poem> userPoems = poemRepo.findByUserIdOrderByCreatedAtDesc(Long.valueOf(user.getId()));
                model.addAttribute("userPoems", userPoems);
                
                // Get counts for dashboard stats
                long totalPoems = userPoems.size();
                long publishedCount = userPoems.stream().mapToLong(p -> p.isPublished() ? 1 : 0).sum();
                long draftCount = totalPoems - publishedCount;
                
                model.addAttribute("totalPoems", totalPoems);
                model.addAttribute("publishedCount", publishedCount);
                model.addAttribute("draftCount", draftCount);
            }
            return "dashboard";
        }
        return "login";
    }
}
