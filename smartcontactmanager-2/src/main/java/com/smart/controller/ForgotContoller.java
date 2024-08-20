package com.smart.controller;

import java.util.Random;

import javax.mail.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

import jakarta.persistence.Access;
import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotContoller {
	Random random =new Random(1000);
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	public EmailService emailService;
	@Autowired
	private UserRepository userRepository;
	@GetMapping("/forgot")
	public String  openEmail() {
		
		return "forgot_email_form";
	}
	
	
	@PostMapping("/send-otp")
	public String  sendOtp(@RequestParam("email") String email,HttpSession session){
		System.out.println("Email "+email);
		
		//generating otp of 4 digit
		
		
		int otp = random.nextInt(9999);
		System.out.println("otp "+otp);
		
		//code for send OTP
		String subject="OTP From SCM";
		String message=""
				+"<div style='border:1px solid #e2e2e2; padding:20px'>"
				+"<h1>"
				+"OTP is "
				+"<b>"+otp
				+"</n>"
				+"</h1>"
				+"</div>";
		String to=email;	
		boolean flag = this.emailService.sendEmail(subject, message, to);
		if(flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email",email);
			return "verify_otp";
		}else {
			
			session.setAttribute("message","Check your email id!!");
			return "forgot_email_form";
		}
		
		
	}
	
	
	//verify otp
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp,HttpSession session) {
		int myOtp=(int)  session.getAttribute("myotp");
		String email=(String)session.getAttribute("email");
		
		if (myOtp==otp) {
			User user = this.userRepository.getUserByUserName(email);
			if(user==null) {
				//error message
				session.setAttribute("message","User Does Not Exit With this Email!!");
				return "forgot_email_form";
			}else {
				
			}
			
			return "password_change_form";
			
			
		}else {
			
			session.setAttribute("message1", "You have Enterd Wrong OTP");
			return "verify_otp";
		}
		
		
	}
	
	//change Password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword,HttpSession session) {
		String email=(String) session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
		this.userRepository.save(user);
		
		return "redirect:/signin?change=password changed Successfully...";
	}

}
