package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.boot.jaxb.mapping.JaxbGenericIdGenerator;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.external.com.google.gdata.util.common.base.PercentEscaper;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import com.razorpay.*;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	
	// common Data 
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String userName = principal.getName();
		System.out.println("UserName"+userName);
		//get the user using username
		User user = userRepository.getUserByUserName(userName);
		System.out.println("User "+user);
		model.addAttribute("user",user);
		
	}
	
	
	//Dashboard Home
	@RequestMapping("/index")
	public String dashboard( Model model) {
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	@GetMapping("/add-contact")
	public String addContactForm(Model model) {
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String addContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,
			Principal principal,HttpSession session) {
	
		try {
			String name=principal.getName();
			 User user=this.userRepository.getUserByUserName(name);
            
			 //processing and uploading file
			 if(file.isEmpty()) {
				 System.out.println("file is empty");
				 contact.setImage("contact.png");
				 
			 }else {
				contact.setImage(file.getOriginalFilename());
				
				File savefile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path , StandardCopyOption.REPLACE_EXISTING);
				System.out.println("image is uploaded");
			}
			 contact.setUser(user);
			 
			 user.getContacts().add(contact);
			 
			 
			 this.userRepository.save(user);
			 
			 System.out.println("Added to data base");
			 
			System.out.println("data"+contact);
			
			//succese message
			session.setAttribute("message",new Message("Yor Conatct is Added!! Add more", "success") );
			
		} catch (Exception e) {
			System.out.println("Error" +e.getMessage());
			e.printStackTrace();
			//error message
			session.setAttribute("message",new Message("Somenting Went Wrong !! Try Again", "danger") );

		}
		return "normal/add_contact_form";
	}
	
	
	//show contact Handler
	
	//pgination
	//per page =7
	//current page=0
	@GetMapping("/show-contacts/{page}")
	public String showContact(@PathVariable("page") Integer page, Model m,Principal principal){
		
		m.addAttribute("title","View Contacts");
  //contact list fetching
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		Pageable pageable = PageRequest.of(page, 5);
		
		Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(),pageable);
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage",page);
		
		m.addAttribute("totalPages",contacts.getTotalPages()); 
		
		return "normal/show_contact";
	} 
	
	//perticular contact Details
	@GetMapping("/contact/{cId}")
	public String showContactDetails(@PathVariable("cId") Integer cId,Model model,Principal principal) {
		System.out.println("CID"  +cId);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName); 
		
		if(user.getId()==contact.getUser().getId()) {
			model.addAttribute("contact",contact);
			model.addAttribute("title",contact.getName());

		}
		return "/normal/contact_detail";
	}
	
	//delete Contact handler
	@GetMapping("/delete/{cId}")
	@Transactional
	public String deleteContact(@PathVariable("cId") Integer cId,Model model,HttpSession session,
			Principal principal) {
		
		Contact contact =this.contactRepository.findById(cId).get();
		  User user = this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		
		this.userRepository.save(user);
		
		/*
		 * this.contactRepository.delete(contact);
		 */		/*
		 * session.setAttribute("message", new Message("Contact deleted Succesfully",
		 * "success"));
		 */		
		return "redirect:/user/show-contacts/0";
	}

	
	
	
	
	//updating Handler
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId,  Model model) {
		model.addAttribute("title","Update Contact");
		Contact contact = this.contactRepository.findById(cId).get();
		model.addAttribute("contact", contact);
		return "normal/update_form";
	}
	

	
	
	
	
	
	
	//update contact handler
	@PostMapping("/process-update")
	public String updatehandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file ,Model m,
			HttpSession session,Principal principal) {
		
		//old contact deteail
		Contact oldContactDetail =  this.contactRepository.findById(contact.getcId()).get();
		
		try {
			if(!file.isEmpty()) {
				//delete old photo
				File deleteFile= new ClassPathResource("static/img").getFile();
                File file1=new  File(deleteFile,oldContactDetail.getImage());
                file1.delete();
				
				//update new photo
				File savefile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path , StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
				
			}else {
				contact.setImage(oldContactDetail.getImage());
			}
			User user=this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		System.out.println("Contact"+contact.getName());
		return "redirect:/user/contact/"+contact.getcId();
	}
	
	//User profile handler
	@GetMapping("/profile")
	public String userProfile(Model model) {
		model.addAttribute("title","Profile Page");
		return "normal/profile";
	}
	
	//open seting handler
	@GetMapping("/settings")
	public String openSetting() {
		return "normal/settings";
	}
	
	
	//change password handler
	@PostMapping("/change-password")
	public String changePasword( @RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,
			Principal principal) {
		System.out.println("Old-Password "+oldPassword);
		System.out.println("new-Password "+newPassword);
		String user = principal.getName();
		User currentUser = userRepository.getUserByUserName(user);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			//change passaword
			
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
		}else {
			//error
			return "redirect:/user/settings";
		}
		return "redirect:/user/index"; 
	}
	 
	
	//crete order for mpayment
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data) throws  Exception {
	//	System.out.println("order function Exrecuted");
		System.out.println("data"+data);
		int amt = Integer.parseInt(data.get("amount").toString());
		var client = new RazorpayClient("rzp_test_3QU3SwUMfBgzff","7lgxHSMfT6HyU7tKgHU2PghI");
		
		JSONObject ob = new JSONObject();
		ob.put("amount", amt*100);
		ob.put("currency", "INR");
		ob.put("receipt", "txn_235425");
		Order order = client.orders.create(ob);
		System.out.println(order);
		return order.toString();
	}
}
