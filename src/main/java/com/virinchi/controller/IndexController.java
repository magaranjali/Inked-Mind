package com.virinchi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.virinchi.model.UserClass;
import com.virinchi.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class IndexController {
	
	@Autowired
    private UserRepository uRepo;
	
	@GetMapping("/")
    public String myFirstPage(Model model, HttpSession session) {
        String activeUser = (String) session.getAttribute("activeUser");
        
        if (activeUser != null) {
            UserClass user = uRepo.findTopByEmailOrderByIdDesc(activeUser);
            if (user != null) {
                model.addAttribute("currentUser", user);
                model.addAttribute("activeUser", activeUser);
            }
        }
        
        return "index"; 
    }
	
	@GetMapping("/index")
    public String indexPage(Model model, HttpSession session) {
        String activeUser = (String) session.getAttribute("activeUser");
        
        if (activeUser != null) {
            UserClass user = uRepo.findTopByEmailOrderByIdDesc(activeUser);
            if (user != null) {
                model.addAttribute("currentUser", user);
                model.addAttribute("activeUser", activeUser);
            }
        }
        
        return "index"; 
    }
	
    
}
 
