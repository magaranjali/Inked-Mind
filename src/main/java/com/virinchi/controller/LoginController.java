package com.virinchi.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.virinchi.model.UserClass;
import com.virinchi.repository.UserRepository;


@Controller
public class LoginController {
	
	@Autowired
	private UserRepository uRepo;

    @GetMapping("/login")
    public String loginPage(HttpSession session, Model model)
    {
        String activeUser = (String) session.getAttribute("activeUser");
        if (activeUser != null) {
            // Ensure we always go through dashboard controller to populate model
            return "redirect:dashboard";
        }
        return "login";
    }
	
	@PostMapping("/login")
	public String postLoginPage(@ModelAttribute UserClass ur, Model m, HttpSession session)
	{
		String email=ur.getEmail();

        String password=ur.getPassword();
		
        if (password == null || password.isEmpty()) {
            m.addAttribute("loginerror", "Password cannot be empty");
            return "login";
        }
        
		String hashPassword= DigestUtils.md5DigestAsHex(password.getBytes());

		boolean result=	uRepo.existsByEmailAndPassword(email, hashPassword);
        if(result==true)
        {
            session.setAttribute("activeUser", email);
            session.setMaxInactiveInterval(20 * 60);
            m.addAttribute("user", uRepo.findTopByEmailOrderByIdDesc(email));
            return "redirect:dashboard";
        }
		else
		{
		m.addAttribute("loginerror","Email or Password Incorrect");
		return "login";
		}
	}
	
	
	@GetMapping("/logout")
	public String logoutPage(HttpSession session){
		session.invalidate();
		return "index";
	}
	
	}
