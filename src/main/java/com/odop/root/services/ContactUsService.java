package com.odop.root.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.odop.root.models.ContactUs;
import com.odop.root.repository.ContactUsRepository;

@Service
public class ContactUsService {
	
	@Autowired
	ContactUsRepository contactUsRepo;
	
	public ContactUs saveContact(ContactUs contactUs) {
		return this.contactUsRepo.save(contactUs);
	}
	
	public List<ContactUs> getAllContactUs() {
		return this.contactUsRepo.findAll();
	}
	
	public Optional<ContactUs> getContactUsById(String contactId) {
		return this.contactUsRepo.findById(contactId);
	}

}
