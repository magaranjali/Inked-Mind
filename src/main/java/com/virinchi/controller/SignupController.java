package com.virinchi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.virinchi.model.UserClass;
import com.virinchi.repository.UserRepository;


@Controller
public class SignupController {


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

    @PostMapping("/signup")
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

        return "login";
    }

}
