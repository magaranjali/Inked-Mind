package com.virinchi.RestControllerTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.virinchi.model.UserClass;
import com.virinchi.repository.UserRepository;



@RestController
@RequestMapping("/api")  
public class SignupRestController {

    @Autowired
    private UserRepository uRepo;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Value("${inkedmind.admin.email:}")
    private String adminEmail;

    @PostMapping("/register")  
    public String signupPagePost(@ModelAttribute UserClass ur) {

        String email = ur.getEmail();
        String password = ur.getPassword();

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            return "Email and Password cannot be empty";
        }

        String hashPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        ur.setPassword(hashPassword);

        uRepo.save(ur);

        return "User registered successfully";
    }
}



