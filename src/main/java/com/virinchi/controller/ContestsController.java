package com.virinchi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpSession;

@Controller
public class ContestsController {
	@GetMapping("/contests")
	public String loginPage()
	{
		return "contests";
	}
	
	@ModelAttribute
	public void addActiveUserToModel(HttpSession session, Model m) {
	    Object activeUser = session.getAttribute("activeUser");
	    m.addAttribute("activeUser", activeUser);
	}


}
