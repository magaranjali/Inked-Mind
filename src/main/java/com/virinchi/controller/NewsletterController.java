package com.virinchi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.virinchi.model.NewsletterSubscription;
import com.virinchi.model.UserClass;
import com.virinchi.repository.NewsletterSubscriptionRepository;
import com.virinchi.repository.PaymentRepository;
import com.virinchi.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping
public class NewsletterController {

    @Autowired
    private NewsletterSubscriptionRepository newsletterRepo;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Value("${inkedmind.admin.email:}")
    private String adminEmail;

    @Autowired
    private UserRepository uRepo;

    @Autowired
    private PaymentRepository paymentRepo;

    @PostMapping("/newsletter/subscribe")
    public String subscribe(@RequestParam("email") String email,
                            HttpServletRequest request,
                            Model model) {
        if (email == null || email.isBlank()) {
            model.addAttribute("newsletterMessage", "Please provide a valid email.");
            return redirectBack(request);
        }

        boolean exists = newsletterRepo.existsByEmailIgnoreCase(email.trim());
        if (!exists) {
            newsletterRepo.save(new NewsletterSubscription(email.trim()));
        }

        // Send confirmation to user
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(email);
            msg.setSubject("Thanks for subscribing – Inked Mind");
            msg.setText("You're now subscribed to Inked Mind's newsletter. We'll keep you posted on new poems, contests and updates.\n\n— Team Inked Mind");
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("[MAIL] Newsletter user email failed: " + e.getMessage());
        }

        // Notify admin
        try {
            String adminTo = (adminEmail != null && !adminEmail.isBlank()) ? adminEmail : mailFrom;
            SimpleMailMessage notify = new SimpleMailMessage();
            notify.setFrom(mailFrom);
            notify.setTo(adminTo);
            notify.setSubject("Inked Mind – New Newsletter Subscription");
            notify.setText("A new user subscribed to the newsletter: " + email);
            mailSender.send(notify);
        } catch (Exception e) {
            System.err.println("[MAIL] Newsletter admin email failed: " + e.getMessage());
        }

        model.addAttribute("newsletterMessage", exists ? "You're already subscribed." : "Subscription successful! Check your inbox.");
        return redirectBack(request);
    }

    /* ===================== Admin Management ===================== */
    @GetMapping("/admin/subscribers")
    public String listSubscribers(HttpServletRequest request, Model model) {
        if (!isAdmin(request)) return "redirect:/login";
        model.addAttribute("subs", newsletterRepo.findAll());
        model.addAttribute("payments", paymentRepo.findAll());
        model.addAttribute("activeAdminTab", "subscribers");
        return "admin-dashboard";
    }

    @PostMapping("/admin/subscribers/add")
    public String addSubscriber(@RequestParam("email") String email, HttpServletRequest request, Model model) {
        if (!isAdmin(request)) return "redirect:/login";
        if (email != null && !email.isBlank() && !newsletterRepo.existsByEmailIgnoreCase(email.trim())) {
            newsletterRepo.save(new NewsletterSubscription(email.trim()));
        }
        return "redirect:/admin/subscribers";
    }

    @PostMapping("/admin/subscribers/update")
    public String updateSubscriber(@RequestParam("id") Long id, @RequestParam("email") String email, HttpServletRequest request) {
        if (!isAdmin(request)) return "redirect:/login";
        if (id != null && email != null && !email.isBlank()) {
            NewsletterSubscription ns = newsletterRepo.findById(id).orElse(null);
            if (ns != null) {
                ns.setEmail(email.trim());
                newsletterRepo.save(ns);
            }
        }
        return "redirect:/admin/subscribers";
    }

    @PostMapping("/admin/subscribers/delete")
    public String deleteSubscriber(@RequestParam("id") Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return "redirect:/login";
        if (id != null) {
            newsletterRepo.deleteById(id);
        }
        return "redirect:/admin/subscribers";
    }

    @GetMapping("/admin/subscribers/export")
    public ResponseEntity<byte[]> exportSubscribers(HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(302).header("Location", "/login").build();
        StringBuilder sb = new StringBuilder();
        sb.append("id,email,subscribedAt\n");
        for (NewsletterSubscription ns : newsletterRepo.findAll()) {
            sb.append(ns.getId() == null ? "" : ns.getId()).append(',')
              .append(ns.getEmail() == null ? "" : ns.getEmail()).append(',')
              .append(ns.getSubscribedAt() == null ? "" : ns.getSubscribedAt()).append('\n');
        }
        byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inkedmind_subscribers.csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(bytes);
    }

    private boolean isAdmin(HttpServletRequest request) {
        Object email = request.getSession().getAttribute("activeUser");
        if (email == null) return false;
        try {
            UserClass user = uRepo.findTopByEmailOrderByIdDesc(String.valueOf(email));
            return user != null && user.getRole() != null && user.getRole().equalsIgnoreCase("Admin");
        } catch (Exception e) {
            return false;
        }
    }

    private String redirectBack(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return "redirect:/index";
        }
        // Ensure we don't redirect outside the app (basic safety)
        if (referer.startsWith("http") && referer.contains(request.getServerName())) {
            return "redirect:" + referer.replaceFirst("https?://[^/]+", "");
        }
        return "redirect:" + referer;
    }
}
