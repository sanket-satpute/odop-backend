package com.odop.root.services;

import java.util.List;
// Optional import removed - not used

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.odop.root.models.Admin;
import com.odop.root.repository.AdminRepository;

@Service
public class AdminService {

	@Autowired
	AdminRepository admin_repository;

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	public Admin saveAdmin(Admin admin) {
		boolean isNewAdmin = admin.getAdminId() == null || admin.getAdminId().isEmpty();
		
		// Generate UUID if not provided (new registration)
		if (isNewAdmin) {
			admin.setAdminId(java.util.UUID.randomUUID().toString());
			
			// Set creation timestamp
			admin.setCreatedAt(java.time.LocalDateTime.now());
			
			// Encode password for new admin
			if (admin.getPassword() != null && !admin.getPassword().isEmpty()) {
				admin.setPassword(passwordEncoder.encode(admin.getPassword()));
			}
			
			// Set default active status
			admin.setActive(true);
		} else {
			// For updates, only encode password if it's a new plain text password
			if (admin.getPassword() != null && !admin.getPassword().isEmpty() 
				&& !admin.getPassword().startsWith("$2a$")) {
				admin.setPassword(passwordEncoder.encode(admin.getPassword()));
			}
		}
		
		// Set roles if not set
		if (admin.getRoles() == null || admin.getRoles().isEmpty()) {
			admin.setRoles(List.of("ROLE_ADMIN"));
		}
		
		// Set update timestamp
		admin.setUpdatedAt(java.time.LocalDateTime.now());
		
		return this.admin_repository.save(admin);
	}
	
	public List<Admin> getAllAdmin() {
		return this.admin_repository.findAll();
	}
	
	public Admin getAdmin(String id) {
		return this.admin_repository.findByAdminId(id);
	}
	
	public Admin getByEmailAddressAndPassword(String emailAddress, String password) {
		Admin admin = admin_repository.findByEmailAddress(emailAddress);
		if (admin != null && passwordEncoder.matches(password, admin.getPassword())) {
			return admin;
		}
		return null;
	}
	
	public Admin getByEmailAndPasswordAndAuthorizationKey(String emailAddress, String password, String authorizationKey) {
		Admin admin = admin_repository.findByEmailAddressAndAuthorizationKey(emailAddress, authorizationKey);
		if (admin != null && passwordEncoder.matches(password, admin.getPassword())) {
			return admin;
		}
		return null;
	}
	
	public boolean deleteById(String adminId) {
		if(this.getAdmin(adminId) != null) {
			this.admin_repository.deleteById(adminId);
			return (this.getAdmin(adminId) == null); // Returns true if successfully deleted
		}
		return false;
	}
	
	public Admin updateAdmin(Admin admin, String adminId) {
		Admin existingAdmin = admin_repository.findById(adminId)
				.orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));

		existingAdmin.setContactNumber(admin.getContactNumber());
		existingAdmin.setEmailAddress(admin.getEmailAddress());
		existingAdmin.setFullName(admin.getFullName());
		existingAdmin.setPositionAndRole(admin.getPositionAndRole());
		// Only update password if a new one is provided
		if (admin.getPassword() != null && !admin.getPassword().isEmpty()) {
			existingAdmin.setPassword(passwordEncoder.encode(admin.getPassword()));
		}
		return this.admin_repository.save(existingAdmin);
	}
	
    // ✅ Service method to check if admin exists
    public boolean checkAdminExists(String email, String authorizationKey) {
        return this.admin_repository.existsByEmailAddressOrAuthorizationKey(email, authorizationKey);
    }
    
    public Admin updateAdminStatus(String id, boolean status) {
	    Admin admin = admin_repository.findById(id).orElseThrow(() -> new RuntimeException("Admin not found"));
	    admin.setActive(status);
	    return admin_repository.save(admin);
	  }
}
