package com.virinchi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpSession;

@Controller
public class PricingController {
	@GetMapping("/pricing")
	public String loginPage(HttpSession session, Model m)
	{
		Object activeUser = session.getAttribute("activeUser");
	    m.addAttribute("activeUser", activeUser);
		return "pricing";
	}
	
	@ModelAttribute
	public void addActiveUserToModel(HttpSession session, Model m) {
	    Object activeUser = session.getAttribute("activeUser");
	    m.addAttribute("activeUser", activeUser);
	}
	
	 

}