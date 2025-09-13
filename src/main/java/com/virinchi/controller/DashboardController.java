package com.virinchi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.virinchi.model.ContestEntry;
import com.virinchi.model.Poem;
import com.virinchi.model.PoemView;
import com.virinchi.model.UserClass;
import com.virinchi.repository.ContestEntryRepository;
import com.virinchi.repository.PoemRepository;
import com.virinchi.repository.PoemViewRepository;
import com.virinchi.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private UserRepository uRepo;
    
    @Autowired
    private PoemRepository poemRepo;
    
    @Autowired
    private ContestEntryRepository contestEntryRepo;
    
    @Autowired
    private com.virinchi.repository.PoemLikeRepository poemLikeRepo;
    
    @Autowired
    private com.virinchi.repository.PoemCommentRepository poemCommentRepo;
    
    @Autowired
    private PoemViewRepository poemViewRepo;

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
                
                // Get user's contest entries
                List<ContestEntry> contestEntries = contestEntryRepo.findByUserIdOrderByCreatedAtDesc(Long.valueOf(user.getId()));
                model.addAttribute("contestEntries", contestEntries);
                
                // Get counts for dashboard stats
                long totalPoems = userPoems.size();
                long publishedCount = userPoems.stream().mapToLong(p -> p.isPublished() ? 1 : 0).sum();
                long draftCount = totalPoems - publishedCount;
                
                // Contest entry counts
                long totalContestEntries = contestEntries.size();
                long submittedEntries = contestEntries.stream().mapToLong(c -> c.getStatus().name().equals("SUBMITTED") ? 1 : 0).sum();
                long draftEntries = totalContestEntries - submittedEntries;
                
                // Engagement counts for user's poems
                java.util.List<Long> poemIds = userPoems.stream().map(Poem::getId).toList();
                long likesReceived = poemIds.isEmpty() ? 0 : poemLikeRepo.countByPoemIdIn(poemIds);
                long commentsReceived = poemIds.isEmpty() ? 0 : poemCommentRepo.countByPoemIdIn(poemIds);
                
                model.addAttribute("totalPoems", totalPoems);
                model.addAttribute("publishedCount", publishedCount);
                model.addAttribute("draftCount", draftCount);
                model.addAttribute("totalContestEntries", totalContestEntries);
                model.addAttribute("submittedEntries", submittedEntries);
                model.addAttribute("draftContestEntries", draftEntries);
                model.addAttribute("likesReceived", likesReceived);
                model.addAttribute("commentsReceived", commentsReceived);
                
                // Get recently viewed poems for this user
                List<PoemView> recentViews = poemViewRepo.findTop5ByUserIdOrderByViewedAtDesc(Long.valueOf(user.getId()));
                List<Poem> recentlyViewedPoems = recentViews.stream()
                    .map(view -> poemRepo.findById(view.getPoemId()).orElse(null))
                    .filter(poem -> poem != null)
                    .limit(5)
                    .toList();
                model.addAttribute("recentlyViewedPoems", recentlyViewedPoems);
            }
            return "dashboard";
        }
        return "login";
    }
}
