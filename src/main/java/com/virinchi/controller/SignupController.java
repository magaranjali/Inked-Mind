package com.virinchi.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.virinchi.model.UserClass;
import com.virinchi.repository.UserRepository;

import jakarta.servlet.http.HttpSession;


@Controller
public class SignupController {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    // Optional: configure admin email in application.properties
    @Value("${inkedmind.admin.email:}")
    private String adminEmail;


    //@Autowired is a primary example of dependency injection in Spring
    //What it does : It provides all the necessary model and controller
    //and any other class information to the REPOSITORY that helps
    //the repository object to perform its task
    @Autowired //Dependency Injection
    private UserRepository uRepo;

    @GetMapping("/signup")
    public String signupPage()
    {
        return "Signup";
    }

    @PostMapping("/register")
    public String signupPagePost(@ModelAttribute UserClass ur)
    {
//@ModelAttribute utilizes the idea of HTTPRequest to
//get the parameters of form data into our controller
//the form data name should be exactly same as the model
//instance variables
        

    	String email=ur.getEmail();
        String password=ur.getPassword();

        String hashPassword= DigestUtils.md5DigestAsHex(password.getBytes());

        ur.setPassword(hashPassword);

        uRepo.save(ur);

        // Send welcome email to user (non-blocking of the flow if it fails)
        try {
            SimpleMailMessage welcome = new SimpleMailMessage();
            welcome.setFrom(mailFrom);
            welcome.setTo(email);
            welcome.setSubject("Welcome to Inked Mind");
            String firstName = (ur.getFirstName() != null && !ur.getFirstName().isBlank()) ? ur.getFirstName() : "Poet";
            welcome.setText("Hi " + firstName + ",\n\n" +
                    "Welcome to Inked Mind! Your account has been created successfully.\n\n" +
                    "You can now log in and start your poetic journey.\n\n" +
                    "— Team Inked Mind");
            mailSender.send(welcome);
        } catch (Exception ex) {
            System.err.println("[MAIL] Failed to send welcome email to user: " + email + ". Reason: " + ex.getMessage());
        }

        // Notify admin of new registration
        try {
            String adminTo = (adminEmail != null && !adminEmail.isBlank()) ? adminEmail : mailFrom;
            SimpleMailMessage notify = new SimpleMailMessage();
            notify.setFrom(mailFrom);
            notify.setTo(adminTo);
            notify.setSubject("Inked Mind – New User Registered");
            StringBuilder body = new StringBuilder();
            body.append("A new user has registered on Inked Mind.\n\n");
            body.append("Name: ").append(ur.getFirstName() != null ? ur.getFirstName() : "").append(" ").append(ur.getLastName() != null ? ur.getLastName() : "").append("\n");
            body.append("Email: ").append(email).append("\n");
            body.append("Role: ").append(ur.getRole() != null ? ur.getRole() : "N/A").append("\n");
            body.append("Registered At: ").append(LocalDateTime.now()).append("\n");
            notify.setText(body.toString());
            mailSender.send(notify);
        } catch (Exception ex) {
            System.err.println("[MAIL] Failed to send admin notification. Reason: " + ex.getMessage());
        }
        			
        return "login";
    }
    
    @ModelAttribute
	public void addActiveUserToModel(HttpSession session, Model m) {
	    Object activeUser = session.getAttribute("activeUser");
	    m.addAttribute("activeUser", activeUser);
	}
    

}
