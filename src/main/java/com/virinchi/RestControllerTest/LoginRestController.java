package com.virinchi.RestControllerTest;


import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.virinchi.model.UserClass;
import com.virinchi.repository.UserRepository;


@RestController
@RequestMapping("/api")
public class LoginRestController {

    @Autowired
    private UserRepository uRepo;

    @PostMapping("/login")
    public String postLoginPage(@ModelAttribute UserClass ur, Model m, HttpSession session) {
        String email = ur.getEmail();
        String password = ur.getPassword();

        if (password == null || password.isEmpty()) {
            m.addAttribute("loginerror", "Password cannot be empty");
            return "Password is empty";
        }

        String hashPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        boolean result = uRepo.existsByEmailAndPassword(email, hashPassword);

        if (result) {
            session.setAttribute("activeUser", email);
            session.setMaxInactiveInterval(20 * 60);
            m.addAttribute("user", uRepo.findTopByEmailOrderByIdDesc(email));
            return "Login Successfull";
        } else {
            m.addAttribute("loginerror", "Email or Password Incorrect");
            return "login fail";
        }
    }
}

