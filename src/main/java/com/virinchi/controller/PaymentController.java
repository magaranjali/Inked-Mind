package com.virinchi.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.virinchi.model.Payment;
import com.virinchi.model.UserClass;
import com.virinchi.repository.PaymentRepository;
import com.virinchi.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class PaymentController {

    @Autowired
    private UserRepository uRepo;

    @Autowired
    private PaymentRepository pRepo;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Value("${inkedmind.admin.email:}")
    private String adminEmail;

    @ModelAttribute
    public void addActiveUserToModel(HttpSession session, Model model) {
        Object activeUser = session.getAttribute("activeUser");
        model.addAttribute("activeUser", activeUser);
    }

    @GetMapping("/payment")
    public String subscriptionPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("activeUser");
        if (email == null) {
            return "redirect:/login"; // if user not logged in
        }

        UserClass user = uRepo.findTopByEmailOrderByIdDesc(email);
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "payment"; // A JSP/HTML page for subscription
    }

    @PostMapping("/subscribe")
    public String processSubscription(HttpSession session, Model model) {
        String email = (String) session.getAttribute("activeUser");
        if (email == null) return "redirect:/login";

        UserClass user = uRepo.findTopByEmailOrderByIdDesc(email);
        if (user != null) {
            user.setSubscribed(true);
            user.setPlanType("BASIC");
            user.setSubscriptionStart(LocalDate.now());
            user.setSubscriptionEnd(LocalDate.now().plusDays(30));

            double amount = 9.99;
            String status = "SUCCESS";
            String transactionId = UUID.randomUUID().toString();

            user.setLastPaymentAmount(amount);
            user.setLastPaymentStatus(status);
            user.setLastTransactionId(transactionId);

            uRepo.save(user);
            System.out.println(" User subscription updated: " + user.getEmail());

            Payment payment = new Payment(user.getEmail(), user.getPlanType(), amount, status, transactionId);
            Payment savedPayment = pRepo.save(payment);
            System.out.println("Payment saved with ID: " + savedPayment.getId());

            // Send emails
            sendPaymentEmails(user.getEmail(), savedPayment);
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/api/payment/complete")
    @ResponseBody
    public String savePayment(@RequestBody Payment payment, HttpSession session) {
        String email = (String) session.getAttribute("activeUser");
        if (email == null) return "USER_NOT_LOGGED_IN";

        UserClass user = uRepo.findTopByEmailOrderByIdDesc(email);
        if (user == null) return "USER_NOT_FOUND";

        try {
            // Update user subscription info (fixed 30-day duration)
            user.setSubscribed(true);
            user.setPlanType(payment.getPlanType());
            user.setSubscriptionStart(LocalDate.now());
            user.setSubscriptionEnd(LocalDate.now().plusDays(30));

            user.setLastPaymentAmount(payment.getAmount());
            user.setLastPaymentStatus(payment.getPaymentStatus());
            user.setLastTransactionId(payment.getTransactionId());
            user.setLastPaymentMethod(payment.getPaymentMethod()); // <-- Add this
            uRepo.save(user);

            // Save payment record with method
            Payment paymentToSave = new Payment();
            paymentToSave.setEmail(email);
            paymentToSave.setPlanType(payment.getPlanType());
            paymentToSave.setAmount(payment.getAmount());
            paymentToSave.setPaymentStatus(payment.getPaymentStatus());
            paymentToSave.setTransactionId(payment.getTransactionId());
            paymentToSave.setPaymentMethod(payment.getPaymentMethod()); // <-- Set method
            if (paymentToSave.getPaymentDate() == null) {
                paymentToSave.setPaymentDate(java.time.LocalDateTime.now());
            }
            Payment savedPayment = pRepo.save(paymentToSave);

            System.out.println("Payment successful for user: " + email + ", Txn ID: " + payment.getTransactionId() + ", Method: " + payment.getPaymentMethod());
            // Send emails
            sendPaymentEmails(email, savedPayment);
            return "SUCCESS";

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }


//    // Check subscription status
//    @GetMapping("/subscription-status")
//    public String subscriptionStatus(HttpSession session, Model model) {
//        String email = (String) session.getAttribute("activeUser");
//        if (email == null) {
//            return "redirect:/login";
//        }
//
//        UserClass user = uRepo.findByEmail(email);
//        if (user == null) {
//            return "redirect:/login";
//        }
//
//        boolean isActive = user.isSubscribed() && user.getSubscriptionEnd() != null
//                && user.getSubscriptionEnd().isAfter(LocalDate.now());
//
//        model.addAttribute("user", user);
//        model.addAttribute("activeSub", isActive);
//
//        return "subscription-status"; // a page to show subscription details
//    }

    private void sendPaymentEmails(String userEmail, Payment payment) {
        try {
            // User email
            SimpleMailMessage userMsg = new SimpleMailMessage();
            userMsg.setFrom(mailFrom);
            userMsg.setTo(userEmail);
            userMsg.setSubject("Inked Mind – Payment Confirmation");
            String dateStr = payment.getPaymentDate() != null ? payment.getPaymentDate().toString() : LocalDate.now().toString();
            userMsg.setText("Thank you for your payment!\n\n" +
                    "Plan: " + safe(payment.getPlanType()) + "\n" +
                    "Amount: $" + payment.getAmount() + "\n" +
                    "Status: " + safe(payment.getPaymentStatus()) + "\n" +
                    "Transaction ID: " + safe(payment.getTransactionId()) + "\n" +
                    "Date: " + dateStr + "\n\n" +
                    "Enjoy your subscription!\n— Team Inked Mind");
            mailSender.send(userMsg);
        } catch (Exception e) {
            System.err.println("[MAIL] Failed to send user payment email: " + e.getMessage());
        }
        try {
            // Admin email
            String adminTo = (adminEmail != null && !adminEmail.isBlank()) ? adminEmail : mailFrom;
            SimpleMailMessage adminMsg = new SimpleMailMessage();
            adminMsg.setFrom(mailFrom);
            adminMsg.setTo(adminTo);
            adminMsg.setSubject("Inked Mind – New Payment Received");
            String dateStr = payment.getPaymentDate() != null ? payment.getPaymentDate().toString() : LocalDate.now().toString();
            adminMsg.setText("A payment was completed.\n\n" +
                    "User: " + userEmail + "\n" +
                    "Plan: " + safe(payment.getPlanType()) + "\n" +
                    "Amount: $" + payment.getAmount() + "\n" +
                    "Status: " + safe(payment.getPaymentStatus()) + "\n" +
                    "Transaction ID: " + safe(payment.getTransactionId()) + "\n" +
                    "Date: " + dateStr + "\n");
            mailSender.send(adminMsg);
        } catch (Exception e) {
            System.err.println("[MAIL] Failed to send admin payment email: " + e.getMessage());
        }
    }

    private String safe(String v) { return v == null ? "" : v; }
}
