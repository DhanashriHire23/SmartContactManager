package com.smart.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

@Service
public class EmailService {
	public boolean sendEmail(String subject,String message,String to) {
		
		  boolean flag=false;
			
			String from="satyamthorat01@gmail.com";
			
			//variable for gemail
			String host="smtp.gmail.com";
			
			//get the system properties
			Properties properties = System.getProperties();
			System.out.println("Properties"+properties);
			
			
			//setting important information to properties obbject 
			
			
			//host set
			properties.put("mail.smtp.host", host);
			properties.put("mail.smtp.port","465");
			properties.put("mail.smtp.ssl.enable","true");
			properties.put("mail.smtp.auth","true");
			
			//Step 1:to get the session object
			Session session=  Session.getInstance(properties,new Authenticator() {

				protected PasswordAuthentication getPasswordAuthentication() {
					// TODO Auto-generated method stub
					return new PasswordAuthentication("satyamthorat01@gmail.com","ywbrfiqyzrnwbmbd");
				}
			
			} );
			
			session.setDebug(true);
			
			//step 2:compose the message[text,multi media]
			
			MimeMessage m = new MimeMessage(session);
			
			try {
				//from email
				m.setFrom(from);
				
				//adding reipient to message
				m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
				
				//adding subject to msasage
				m.setSubject(subject);
				
				
				//adding text to message
		//		m.setText(message);
				m.setContent(message,"text/html");
				
				
				//attachment
				
				
				
				//file path
				String path="G:\\Geeta-ke-shalok.jpg";
				
				MimeMultipart mimeMultipart = new MimeMultipart();
				
				//text
				
				//file
				
				MimeBodyPart textMime = new MimeBodyPart();
				
				MimeBodyPart fileMime = new MimeBodyPart();
				
				/*
				 * try { textMime.setText(message);
				 * 
				 * File file=new File(path); fileMime.attachFile(file);
				 * 
				 * 
				 * mimeMultipart.addBodyPart(textMime); mimeMultipart.addBodyPart(fileMime);
				 * 
				 * } catch (Exception e) { e.printStackTrace(); }
				 */
				
				//m.setContent(mimeMultipart);
				
				//send
				
				//step 3:send the message using transport class
				Transport.send(m);
				System.out.println("Sent success......................");
				flag=true;
				
				
				
			} catch (Exception e) {
	           e.printStackTrace();   
			}
			return flag;
			
		}

}
