package com.user.service;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.user.dto.UserDetailsDto;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMessage.RecipientType;
@Service
public class EmailService {
	
	@Autowired
	Environment env;
	
	public void sendPasswordForNewUser(UserDetailsDto userDetailsDto) {

		String toMailAddress = userDetailsDto.getEmail();

		String mailSubject = env.getProperty("passwordSubject");
		String mailContent = env.getProperty("passwordContent");
		String approval = env.getProperty("loginUri");

		String modifiedContent = mailContent
				.replace("[password]", userDetailsDto.getPassword())
				.replace("[link]", approval);

		sendMail(toMailAddress, mailSubject, modifiedContent);
	}
	
	@Async("threadPoolTaskExecutor")
	public String sendMail(String toMailAddress, String subject, String content) {

		Message message = new MimeMessage(getSession());

		String sendMail = env.getProperty("sendMail");
		if (sendMail == null || !sendMail.equalsIgnoreCase("yes")) {
			System.out.println("MAIL IGNORED");
			return "SENT";
		} else {

			try {
				message.addRecipient(RecipientType.TO, new InternetAddress(toMailAddress));
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			try {
				String fromAddress = "";
				fromAddress = env.getProperty("fromAddress");
				message.addFrom(new InternetAddress[] { new InternetAddress(fromAddress) });
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}

//			try {
//				String ccAllMail = "";
//				ccAllMail = env.getProperty("ccAddress");
//				String[] ccMails = ccAllMail.split(",");
//				for (String cc : ccMails) {
//					message.addRecipient(RecipientType.CC, new InternetAddress(cc));
//				}
//			} catch (AddressException e) {
//				e.printStackTrace();
//			} catch (MessagingException e) {
//				e.printStackTrace();
//			}

			try {
				message.setSubject(subject);
				message.setContent(content, "text/html");
				/* message.setContent(content, "text/plain"); */
				Transport.send(message);
			} catch (MessagingException e) {
				e.printStackTrace();
			}

			System.out.println("SENT");

			return "SENT";
		}
	}

	private Session getSession() {

		String hostName = "";
		String portNumber = "";

		hostName = env.getProperty("hostName");
		portNumber = env.getProperty("portNumber");

		Authenticator authenticator = new Authenticator();

		Properties properties = new Properties();
		properties.setProperty("mail.smtp.submitter", authenticator.getPasswordAuthentication().getUserName());
		properties.setProperty("mail.smtp.auth", "true");
		properties.setProperty("mail.smtp.host", hostName);
		properties.setProperty("mail.smtp.port", portNumber);
		properties.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
		properties.setProperty("mail.smtp.starttls.enable", "true");

		return Session.getInstance(properties, authenticator);
	}
	
	private class Authenticator extends jakarta.mail.Authenticator {
		private PasswordAuthentication authentication;

		public Authenticator() {

			String fromEmail = env.getProperty("authAddress");
			String fromEmailPwd = env.getProperty("fromEmailPwd");

			authentication = new PasswordAuthentication(fromEmail, fromEmailPwd);
		}

		protected PasswordAuthentication getPasswordAuthentication() {
			return authentication;
		}
	}
}
