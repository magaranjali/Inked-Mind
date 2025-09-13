package com.virinchi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.virinchi.model.ContestEntry;
import com.virinchi.model.ContestEntry.EntryStatus;
import com.virinchi.model.ContestEntry.ContestType;
import com.virinchi.model.UserClass;
import com.virinchi.repository.ContestEntryRepository;
import com.virinchi.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class ContestsController {
	
	@Autowired
	private UserRepository uRepo;
	
	@Autowired
	private ContestEntryRepository contestEntryRepo;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Value("${inkedmind.admin.email:}")
    private String adminEmail;
	
    @GetMapping("/contests")
    public String contestsPage(HttpSession session, Model model)
    {
        String email = (String) session.getAttribute("activeUser");
        if (email != null) {
            UserClass user = uRepo.findTopByEmailOrderByIdDesc(email);
            model.addAttribute("currentUser", user);
            
            // Check if user is a reader - readers cannot access contests
            if (user != null && isReader(user)) {
                return "redirect:/poems?error=readers-cannot-join-contests";
            }
            
            if (user != null) {
                java.util.List<ContestEntry> entries = contestEntryRepo.findByUserIdOrderByCreatedAtDesc(Long.valueOf(user.getId()));
                model.addAttribute("myContestEntries", entries);
            }
        }
        return "contests";
    }
	
	@GetMapping("/contest-submission")
	public String contestSubmissionPage(@RequestParam(value = "id", defaultValue = "1") String contestId, Model model, HttpSession session)
	{
		// Check if user is logged in via session
		String activeUser = (String) session.getAttribute("activeUser");
		if (activeUser == null) {
			// Redirect to login page if not authenticated
			return "redirect:/login";
		}
		
		// Get the full user object from database
		UserClass user = uRepo.findTopByEmailOrderByIdDesc(activeUser);

        // Check if user is a reader - readers cannot access contest submissions
        if (user != null && isReader(user)) {
            return "redirect:/poems?error=readers-cannot-join-contests";
        }

        // Enforce access to contest by type
        ContestType type = getContestTypeById(contestId);
        if (type == ContestType.PREMIUM && !hasPremiumAccess(user)) {
            return "redirect:/pricing";
        }
		
		model.addAttribute("contestId", contestId);
		model.addAttribute("contestType", type.name());
		model.addAttribute("activeUser", activeUser);
		model.addAttribute("currentUser", user);
		return "contest-submission";
	}

    @PostMapping("/contests/join")
    public String joinContest(@RequestParam("contestId") String contestId,
                              @RequestParam(value = "type", defaultValue = "BASIC") String contestType,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        String activeUser = (String) session.getAttribute("activeUser");
        if (activeUser == null) return "redirect:/login";

        UserClass user = uRepo.findTopByEmailOrderByIdDesc(activeUser);
        if (user == null) return "redirect:/login";
        
        // Check if user is a reader - readers cannot join contests
        if (isReader(user)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Readers cannot join contests. Please upgrade your account to participate.");
            return "redirect:/poems";
        }

        ContestType resolvedType = getContestTypeById(contestId);
        // Do not trust client-provided type; use resolved
        if (resolvedType == ContestType.PREMIUM && !hasPremiumAccess(user)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Premium membership required to enter this contest.");
            return "redirect:/pricing";
        }

        try {
            // Avoid duplicate draft entries for the same contest
            java.util.Optional<ContestEntry> existing = contestEntryRepo
                    .findByUserIdAndContestIdAndStatus(Long.valueOf(user.getId()), contestId, EntryStatus.DRAFT);
            ContestEntry entry = existing.orElseGet(() -> new ContestEntry(
                    contestId,
                    "Joined Contest",
                    "",
                    Long.valueOf(user.getId()),
                    user.getFirstName() + " " + user.getLastName(),
                    EntryStatus.DRAFT,
                    resolvedType
            ));
            entry.setContestType(resolvedType);
            contestEntryRepo.save(entry);

            // Email notifications
            sendContestJoinEmails(user, contestId, resolvedType);

            redirectAttributes.addFlashAttribute("successMessage", "You have joined the contest. Good luck!");
            return "redirect:/contest-submission?id=" + contestId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not join the contest. Please try again.");
            return "redirect:/contests";
        }
    }
	
	@PostMapping("/contest-submission/submit")
    public String submitEntry(@RequestParam String contestId,
                             @RequestParam String title,
                             @RequestParam String content,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
		
		String activeUser = (String) session.getAttribute("activeUser");
		if (activeUser == null) {
			return "redirect:/login";
		}
		
		UserClass user = uRepo.findTopByEmailOrderByIdDesc(activeUser);
		if (user == null) {
			return "redirect:/login";
		}
		
		// Check if user is a reader - readers cannot submit to contests
		if (isReader(user)) {
			redirectAttributes.addFlashAttribute("errorMessage", "Readers cannot submit to contests. Please upgrade your account to participate.");
			return "redirect:/poems";
		}
		
        try {
            ContestType type = getContestTypeById(contestId);
            if (type == ContestType.PREMIUM && !hasPremiumAccess(user)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Premium membership required to submit to this contest.");
                return "redirect:/pricing";
            }
            ContestEntry entry = new ContestEntry(
                contestId,
                title,
                content,
                Long.valueOf(user.getId()),
                user.getFirstName() + " " + user.getLastName(),
                EntryStatus.SUBMITTED,
                type
            );
            
            contestEntryRepo.save(entry);
            // Notify submit
            sendContestSubmitEmails(user, contestId, type);
            redirectAttributes.addFlashAttribute("successMessage", "Your entry has been submitted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to submit entry. Please try again.");
        }
		
		return "redirect:/contests";
	}
	
	@PostMapping("/contest-submission/draft")
	public String saveDraft(@RequestParam String contestId,
	                       @RequestParam String title,
	                       @RequestParam String content,
	                       HttpSession session,
	                       RedirectAttributes redirectAttributes) {
		
		String activeUser = (String) session.getAttribute("activeUser");
		if (activeUser == null) {
			return "redirect:/login";
		}
		
		UserClass user = uRepo.findTopByEmailOrderByIdDesc(activeUser);
		if (user == null) {
			return "redirect:/login";
		}
		
		// Check if user is a reader - readers cannot save contest drafts
		if (isReader(user)) {
			redirectAttributes.addFlashAttribute("errorMessage", "Readers cannot participate in contests. Please upgrade your account.");
			return "redirect:/poems";
		}
		
        try {
            ContestType type = getContestTypeById(contestId);
            ContestEntry entry = new ContestEntry(
                contestId,
                title,
                content,
                Long.valueOf(user.getId()),
                user.getFirstName() + " " + user.getLastName(),
                EntryStatus.DRAFT,
                type
            );
            
            contestEntryRepo.save(entry);
            redirectAttributes.addFlashAttribute("successMessage", "Your entry has been saved as draft!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to save draft. Please try again.");
        }
		
		return "redirect:/contests";
    }

    private ContestType getContestTypeById(String contestId) {
        // Simple mapping for demo: 1 & 2 are BASIC, others PREMIUM
        if (contestId == null) return ContestType.BASIC;
        return ("1".equals(contestId) || "2".equals(contestId)) ? ContestType.BASIC : ContestType.PREMIUM;
    }

    private boolean hasPremiumAccess(UserClass user) {
        if (user == null) return false;
        if (!user.isSubscribed()) return false;
        String plan = user.getPlanType() == null ? "" : user.getPlanType().trim().toUpperCase();
        return plan.equals("PREMIUM") || plan.equals("PRO");
    }

    private void sendContestJoinEmails(UserClass user, String contestId, ContestType type) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(user.getEmail());
            msg.setSubject("Inked Mind – Contest Joined");
            String body = String.format(
                "Hi %s,\n\nYou have joined the %s contest (ID: %s).\nRole: %s\nPlan: %s\n\nYou can finish your submission anytime from your dashboard.\n\n— Team Inked Mind",
                safe(user.getFirstName()), type.name(), contestId, safe(user.getRole()), safe(user.getPlanType())
            );
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("[MAIL] Failed to send join email: " + e.getMessage());
        }
        try {
            String adminTo = (adminEmail != null && !adminEmail.isBlank()) ? adminEmail : mailFrom;
            SimpleMailMessage admin = new SimpleMailMessage();
            admin.setFrom(mailFrom);
            admin.setTo(adminTo);
            admin.setSubject("Inked Mind – Contest Join");
            admin.setText(String.format("User %s joined %s contest (ID: %s)", safe(user.getEmail()), type.name(), contestId));
            mailSender.send(admin);
        } catch (Exception e) {
            System.err.println("[MAIL] Failed to notify admin of join: " + e.getMessage());
        }
    }

    private void sendContestSubmitEmails(UserClass user, String contestId, ContestType type) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(user.getEmail());
            msg.setSubject("Inked Mind – Submission Received");
            msg.setText(String.format(
                "Thanks for your submission to the %s contest (ID: %s).\nRole: %s\nPlan: %s\n\nWe’ll notify you once it’s reviewed.\n\n— Team Inked Mind",
                type.name(), contestId, safe(user.getRole()), safe(user.getPlanType())
            ));
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("[MAIL] Failed to send submit email: " + e.getMessage());
        }
        try {
            String adminTo = (adminEmail != null && !adminEmail.isBlank()) ? adminEmail : mailFrom;
            SimpleMailMessage admin = new SimpleMailMessage();
            admin.setFrom(mailFrom);
            admin.setTo(adminTo);
            admin.setSubject("Inked Mind – New Contest Submission");
            admin.setText(String.format("User %s submitted to %s contest (ID: %s)", safe(user.getEmail()), type.name(), contestId));
            mailSender.send(admin);
        } catch (Exception e) {
            System.err.println("[MAIL] Failed to notify admin of submission: " + e.getMessage());
        }
    }

    private String safe(String v) { return v == null ? "" : v; }
    
    private boolean isReader(UserClass user) {
        if (user == null || user.getRole() == null) return false;
        return "reader".equalsIgnoreCase(user.getRole().trim());
    }
	
	@ModelAttribute
	public void addActiveUserToModel(HttpSession session, Model m) {
	    String activeUser = (String) session.getAttribute("activeUser");
	    m.addAttribute("activeUser", activeUser);
	    
	    if (activeUser != null) {
	        UserClass user = uRepo.findTopByEmailOrderByIdDesc(activeUser);
	        m.addAttribute("currentUser", user);
	    }
	}


}
