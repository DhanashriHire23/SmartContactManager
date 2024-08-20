package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;


@Controller
public class HomeController {
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	
	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title","Home-Samrt Contact Manager");
		return "home";
	}
	
	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("title","About-Samrt Contact Manager");
		return "about";
	}
	
	@GetMapping("/signup")
	public String signup(Model model) { 
		model.addAttribute("title","SignUp-Samrt Contact Manager");
		model.addAttribute("user", new User());
		
		return "signup";
	}
	
	
	//Hander For registering user
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1, @RequestParam(value="agreement",defaultValue = "false") boolean agreement ,Model model,
			HttpSession session) { 
		try {
			if (!agreement) {
				System.out.println("You Have Not Agrre Tem And Condition");
				throw new Exception("You Have Not Agrre Tem And Condition"); 
			}
			
			if(result1.hasErrors()) {
				System.out.println("ERROR "+result1.toString());
				model.addAttribute("user",user);
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnable(true);        
			user.setImgUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			
			System.out.println("Agreement" +agreement);
			System.out.println("User"+user);
			
			User result = this.userRepository.save(user);
			
			model.addAttribute("user",new User());
			session.setAttribute("message", new Message("Succefully Register", "alert-primary"));
			return "signup";
			
		} catch (Exception e) {
			
			
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message", new Message("Something Went Wrong !!"+e.getMessage(), "alert-danger"));
			return "signup";  
		}
		
		
	}

	
	//Hander for custome logine
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title","Logine Page");
		return "login";
	}
	
}
