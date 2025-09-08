package com.virinchi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

import com.virinchi.model.UserClass;
import com.virinchi.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class GlobalModelAttributes {

    @Autowired
    private UserRepository uRepo;

    @ModelAttribute
    public void injectUserIntoModel(HttpSession session, Model model) {
        String email = (String) session.getAttribute("activeUser");
        if (email != null) {
            model.addAttribute("activeUser", email);
            try {
                UserClass user = uRepo.findTopByEmailOrderByIdDesc(email);
                model.addAttribute("currentUser", user);
            } catch (Exception e) {
                model.addAttribute("currentUser", null);
            }
        } else {
            model.addAttribute("activeUser", null);
            model.addAttribute("currentUser", null);
        }
    }
}

