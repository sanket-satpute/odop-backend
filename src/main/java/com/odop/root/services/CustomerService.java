package com.odop.root.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.odop.root.models.Customer;
import com.odop.root.repository.CustomerRepository;

@Service
public class CustomerService {

	@Autowired
	CustomerRepository custRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	public Customer saveCustomer(Customer customer) {
		boolean isNewCustomer = customer.getCustomerId() == null || customer.getCustomerId().isEmpty();
		
        // Generate UUID if not provided (new registration)
        if (isNewCustomer) {
            customer.setCustomerId(UUID.randomUUID().toString());
            
            // Validate unique email and phone only for new registrations
            if (existsByEmailAddressOrContactNumber(customer.getEmailAddress(), customer.getContactNumber())) {
                throw new RuntimeException("Email or phone number already exists");
            }
            
            // Set creation timestamp
            customer.setCreatedAt(java.time.LocalDateTime.now());
            
            // Encode password for new registration
            if (customer.getPassword() != null && !customer.getPassword().isEmpty()) {
                customer.setPassword(passwordEncoder.encode(customer.getPassword()));
            }
            
            // Set default roles
            customer.setRoles(List.of("ROLE_CUSTOMER"));
        } else {
            // For updates, only encode password if it's a new plain text password
            if (customer.getPassword() != null && !customer.getPassword().isEmpty() 
                && !customer.getPassword().startsWith("$2a$")) {
                customer.setPassword(passwordEncoder.encode(customer.getPassword()));
            }
        }
        
        // Set update timestamp
        customer.setUpdatedAt(java.time.LocalDateTime.now());
        
        // Set default status if not set
        if (customer.getStatus() == null) {
            customer.setStatus("active");
        }
        
        return this.custRepo.save(customer);
    }
	
	public List<Customer> getAllCustomer() {
		return this.custRepo.findAll();
	}
	
	public Customer getCustomerById(String customerId) {
		return this.custRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
	}
	
	public Customer getCustomerByEmailAndPassword(String email, String password) {
		Customer customer = this.custRepo.findByEmailAddress(email);
		if (customer != null && passwordEncoder.matches(password, customer.getPassword())) {
			return customer;
		}
		return null;
	}
	
	public boolean deleteById(String customerId) {
		try {
            Customer customer = getCustomerById(customerId);
            this.custRepo.delete(customer);
            return true;
        } catch (Exception e) {
            return false;
        }
	}
	
	public boolean existsByEmailAddressOrContactNumber(String emailAddress, long contactNumber) {
		System.out.println("DEBUG [Service]: existsByEmailAddressOrContactNumber called");
		System.out.println("DEBUG [Service]: Email: " + emailAddress + ", Phone: " + contactNumber);

		boolean result = this.custRepo.existsByEmailAddressOrContactNumber(emailAddress, contactNumber);
		System.out.println("DEBUG [Service]: Repository returned: " + result);

		return result;
	}

	public Customer updateCustomer(Customer customer, String customerId) {
		Customer existingCustomer = custRepo.findById(customerId)
				.orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

		existingCustomer.setFullName(customer.getFullName());
		existingCustomer.setEmailAddress(customer.getEmailAddress());
		existingCustomer.setContactNumber(customer.getContactNumber());
		existingCustomer.setAddress(customer.getAddress());
		existingCustomer.setCity(customer.getCity());
		existingCustomer.setState(customer.getState());
		existingCustomer.setPinCode(customer.getPinCode());
		existingCustomer.setStatus(customer.getStatus());
		existingCustomer.setUpdatedAt(java.time.LocalDateTime.now());

		if (customer.getPassword() != null && !customer.getPassword().isEmpty()) {
			existingCustomer.setPassword(passwordEncoder.encode(customer.getPassword()));
		}
		return custRepo.save(existingCustomer);
	}
	
	public Customer updateCustomerStatus(String id, String status) {
	    Customer customer = getCustomerById(id);
	    if (!status.matches("active|inactive|banned")) {
            throw new IllegalArgumentException("Invalid status value. Must be: active, inactive, or banned");
        }
	    customer.setStatus(status);
	    customer.setUpdatedAt(java.time.LocalDateTime.now());
	    return custRepo.save(customer);
	}
}
