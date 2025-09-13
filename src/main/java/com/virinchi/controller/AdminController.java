package com.virinchi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.virinchi.model.ContestEntry;
import com.virinchi.model.Poem;
import com.virinchi.model.UserClass;
import com.virinchi.repository.ContestEntryRepository;
import com.virinchi.repository.NewsletterSubscriptionRepository;
import com.virinchi.repository.PaymentRepository;
import com.virinchi.repository.PoemRepository;
import com.virinchi.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

    @Autowired
    private UserRepository uRepo;

    @Autowired
    private NewsletterSubscriptionRepository newsletterRepo;

    @Autowired
    private PaymentRepository paymentRepo;
    
    @Autowired
    private ContestEntryRepository contestEntryRepo;
    
    @Autowired
    private PoemRepository poemRepo;

    @GetMapping("/admin/login")
    public String adminLoginPage(HttpSession session) {
        String activeUser = (String) session.getAttribute("activeUser");
        if (activeUser != null) {
            UserClass user = uRepo.findTopByEmailOrderByIdDesc(activeUser);
            if (user != null && user.getRole() != null && user.getRole().equalsIgnoreCase("Admin")) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/dashboard";
        }
        return "admin-login";
    }

    @PostMapping("/admin/login")
    public String adminLoginPost(@ModelAttribute UserClass ur, Model m, HttpSession session) {
        String email = ur.getEmail();
        String password = ur.getPassword();

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            m.addAttribute("loginerror", "Email and Password are required");
            return "admin-login";
        }

        String hashPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        UserClass user = uRepo.findTopByEmailOrderByIdDesc(email);
        if (user != null && user.getPassword() != null && user.getPassword().equals(hashPassword)
                && user.getRole() != null && user.getRole().equalsIgnoreCase("Admin")) {
            session.setAttribute("activeUser", email);
            session.setMaxInactiveInterval(20 * 60);
            return "redirect:/admin/dashboard";
        }
        m.addAttribute("loginerror", "Invalid admin credentials");
        return "admin-login";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model m, 
                                @RequestParam(value = "tab", defaultValue = "overview") String tab) {
        String activeUser = (String) session.getAttribute("activeUser");
        if (activeUser == null) return "redirect:/admin/login";
        UserClass user = uRepo.findTopByEmailOrderByIdDesc(activeUser);
        if (user == null || user.getRole() == null || !user.getRole().equalsIgnoreCase("Admin")) {
            return "redirect:/login";
        }
        
        // Get all contest entries with user information
        java.util.List<ContestEntry> allContestEntries = contestEntryRepo.findAllByOrderByCreatedAtDesc();
        
        // Calculate contest statistics
        long totalContestEntries = allContestEntries.size();
        long submittedEntries = allContestEntries.stream().mapToLong(c -> c.getStatus().name().equals("SUBMITTED") ? 1 : 0).sum();
        long draftEntries = totalContestEntries - submittedEntries;
        
        // Get all poems for admin management
        java.util.List<Poem> allPoems = poemRepo.findAll();
        long totalPoems = allPoems.size();
        long publishedPoems = allPoems.stream().mapToLong(p -> p.isPublished() ? 1 : 0).sum();
        long draftPoems = totalPoems - publishedPoems;
        
        // Create a map to get user information for each poem
        java.util.Map<Long, UserClass> userMap = new java.util.HashMap<>();
        for (Poem poem : allPoems) {
            if (poem.getUserId() != null && !userMap.containsKey(poem.getUserId())) {
                UserClass poemAuthor = uRepo.findById(poem.getUserId().intValue()).orElse(null);
                if (poemAuthor != null) {
                    userMap.put(poem.getUserId(), poemAuthor);
                }
            }
        }
        
        // Add all necessary data for all tabs
        m.addAttribute("subs", newsletterRepo.findAll());
        m.addAttribute("payments", paymentRepo.findAll());
        m.addAttribute("users", uRepo.findAll());
        m.addAttribute("contestEntries", allContestEntries);
        m.addAttribute("totalContestEntries", totalContestEntries);
        m.addAttribute("submittedEntries", submittedEntries);
        m.addAttribute("draftEntries", draftEntries);
        m.addAttribute("allPoems", allPoems);
        m.addAttribute("userMap", userMap);
        m.addAttribute("totalPoems", totalPoems);
        m.addAttribute("publishedPoems", publishedPoems);
        m.addAttribute("draftPoems", draftPoems);
        m.addAttribute("activeAdminTab", tab);
        return "admin-dashboard";
    }

    @GetMapping("/admin/subscriptions")
    public String subscriptions(HttpSession session) {
        String activeUser = (String) session.getAttribute("activeUser");
        if (activeUser == null) return "redirect:/admin/login";
        UserClass user = uRepo.findTopByEmailOrderByIdDesc(activeUser);
        if (user == null || user.getRole() == null || !user.getRole().equalsIgnoreCase("Admin")) {
            return "redirect:/login";
        }
        return "redirect:/admin/dashboard/subscriptions";
    }

    @GetMapping("/admin/subscriptions/export/csv")
    public org.springframework.http.ResponseEntity<byte[]> exportSubscriptionsCsv(HttpSession session) {
        String activeUser = (String) session.getAttribute("activeUser");
        if (activeUser == null) return org.springframework.http.ResponseEntity.status(302).header("Location", "/admin/login").build();
        StringBuilder sb = new StringBuilder();
        sb.append("id,email,plan,amount,status,transactionId,date\n");
        for (com.virinchi.model.Payment p : paymentRepo.findAll()) {
            sb.append(p.getId()).append(',')
              .append(nullToEmpty(p.getEmail())).append(',')
              .append(nullToEmpty(p.getPlanType())).append(',')
              .append(p.getAmount()).append(',')
              .append(nullToEmpty(p.getPaymentStatus())).append(',')
              .append(nullToEmpty(p.getTransactionId())).append(',')
              .append(p.getPaymentDate() == null ? "" : p.getPaymentDate().toString())
              .append('\n');
        }
        byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=subscriptions.csv")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(bytes);
    }

    @GetMapping("/admin/subscriptions/export/png")
    public org.springframework.http.ResponseEntity<byte[]> exportSubscriptionsPng(HttpSession session) throws Exception {
        String activeUser = (String) session.getAttribute("activeUser");
        if (activeUser == null) return org.springframework.http.ResponseEntity.status(302).header("Location", "/admin/login").build();
        java.util.List<com.virinchi.model.Payment> list = paymentRepo.findAll();
        int rowHeight = 24;
        int padding = 10;
        int width = 1200;
        int headerHeight = 40;
        int height = headerHeight + padding + (list.size() + 2) * rowHeight + padding;
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setColor(new java.awt.Color(255, 255, 255, 255));
        g.fillRect(0, 0, width, height);
        g.setColor(java.awt.Color.BLACK);
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 16));
        g.drawString("Inked Mind – Subscriptions", padding, padding + 20);
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
        int y = headerHeight;
        String[] headers = {"ID", "Email", "Plan", "Amount", "Status", "Txn ID", "Date"};
        int[] cols = {40, 260, 120, 80, 100, 300, 260};
        int x = padding;
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        for (int i = 0; i < headers.length; i++) { g.drawString(headers[i], x, y); x += cols[i]; }
        y += rowHeight;
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
        for (com.virinchi.model.Payment p : list) {
            x = padding;
            String[] row = {
                String.valueOf(p.getId()),
                nullToEmpty(p.getEmail()),
                nullToEmpty(p.getPlanType()),
                String.valueOf(p.getAmount()),
                nullToEmpty(p.getPaymentStatus()),
                nullToEmpty(p.getTransactionId()),
                p.getPaymentDate() == null ? "" : p.getPaymentDate().toString()
            };
            for (int i = 0; i < row.length; i++) { g.drawString(row[i], x, y); x += cols[i]; }
            y += rowHeight;
        }
        g.dispose();
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(img, "png", baos);
        byte[] bytes = baos.toByteArray();
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=subscriptions.png")
                .contentType(org.springframework.http.MediaType.IMAGE_PNG)
                .body(bytes);
    }

    private String nullToEmpty(String s) { return s == null ? "" : s; }

    @GetMapping("/admin/users")
    public String adminUsers(HttpSession session) {
        String activeUser = (String) session.getAttribute("activeUser");
        if (activeUser == null) return "redirect:/admin/login";
        UserClass user = uRepo.findTopByEmailOrderByIdDesc(activeUser);
        if (user == null || user.getRole() == null || !user.getRole().equalsIgnoreCase("Admin")) {
            return "redirect:/login";
        }
        return "redirect:/admin/dashboard/users";
    }

    @PostMapping("/admin/users/update-role")
    public String updateUserRole(@ModelAttribute UserClass payload, HttpSession session) {
        String activeUser = (String) session.getAttribute("activeUser");
        if (activeUser == null) return "redirect:/admin/login";
        UserClass admin = uRepo.findTopByEmailOrderByIdDesc(activeUser);
        if (admin == null || admin.getRole() == null || !admin.getRole().equalsIgnoreCase("Admin")) {
            return "redirect:/login";
        }
        if (payload.getId() != 0 && payload.getRole() != null) {
            UserClass u = uRepo.findById(payload.getId()).orElse(null);
            if (u != null) {
                u.setRole(payload.getRole());
                uRepo.save(u);
            }
        }
        return "redirect:/admin/dashboard/users";
    }

    @PostMapping("/admin/users/reset-password")
    public String resetUserPassword(@ModelAttribute UserClass payload, HttpSession session) {
        String activeUser = (String) session.getAttribute("activeUser");
        if (activeUser == null) return "redirect:/admin/login";
        UserClass admin = uRepo.findTopByEmailOrderByIdDesc(activeUser);
        if (admin == null || admin.getRole() == null || !admin.getRole().equalsIgnoreCase("Admin")) {
            return "redirect:/login";
        }
        if (payload.getId() != 0 && payload.getPassword() != null && !payload.getPassword().isBlank()) {
            UserClass u = uRepo.findById(payload.getId()).orElse(null);
            if (u != null) {
                String hash = DigestUtils.md5DigestAsHex(payload.getPassword().getBytes());
                u.setPassword(hash);
                uRepo.save(u);
            }
        }
        return "redirect:/admin/dashboard/users";
    }

    @GetMapping("/admin/poems")
    public String adminPoems(HttpSession session) {
        String activeUser = (String) session.getAttribute("activeUser");
        if (activeUser == null) return "redirect:/admin/login";
        UserClass user = uRepo.findTopByEmailOrderByIdDesc(activeUser);
        if (user == null || user.getRole() == null || !user.getRole().equalsIgnoreCase("Admin")) {
            return "redirect:/login";
        }
        return "redirect:/admin/dashboard/poems";
    }
    
    @PostMapping("/admin/poems/publish")
    public String togglePoemPublish(@ModelAttribute Poem payload, HttpSession session) {
        String activeUser = (String) session.getAttribute("activeUser");
        if (activeUser == null) return "redirect:/admin/login";
        UserClass admin = uRepo.findTopByEmailOrderByIdDesc(activeUser);
        if (admin == null || admin.getRole() == null || !admin.getRole().equalsIgnoreCase("Admin")) {
            return "redirect:/login";
        }
        
        if (payload.getId() != null) {
            Poem poem = poemRepo.findById(payload.getId()).orElse(null);
            if (poem != null) {
                poem.setPublished(!poem.isPublished());
                poemRepo.save(poem);
            }
        }
        return "redirect:/admin/dashboard/poems";
    }
    
    @PostMapping("/admin/poems/delete")
    public String deletePoemAdmin(@ModelAttribute Poem payload, HttpSession session) {
        String activeUser = (String) session.getAttribute("activeUser");
        if (activeUser == null) return "redirect:/admin/login";
        UserClass admin = uRepo.findTopByEmailOrderByIdDesc(activeUser);
        if (admin == null || admin.getRole() == null || !admin.getRole().equalsIgnoreCase("Admin")) {
            return "redirect:/login";
        }
        
        if (payload.getId() != null) {
            poemRepo.deleteById(payload.getId());
        }
        return "redirect:/admin/dashboard/poems";
    }

    @GetMapping("/admin/logout")
    public String adminLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }
}
