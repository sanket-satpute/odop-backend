package com.odop.root.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.odop.root.dto.AdminDto;
import com.odop.root.dto.AdminRegistrationDto;
import com.odop.root.models.Admin;
import com.odop.root.services.AdminService;

@RestController
@RequestMapping("odop/admin")
@CrossOrigin
public class AdminController {

    @Autowired
    private AdminService adminService;
    private static final Logger logger = LogManager.getLogger(AdminController.class);

    @PostMapping("/create_account")
    public ResponseEntity<AdminDto> createAdminAccount(@RequestBody AdminRegistrationDto registrationDto) {
        Admin admin = toEntity(registrationDto);
        Admin savedAdmin = adminService.saveAdmin(admin);
        return ResponseEntity.ok(toDto(savedAdmin));
    }

    @GetMapping("/check_admin_exists")
    public ResponseEntity<Boolean> checkAdminExists(
            @RequestParam String emailAddress,
            @RequestParam String authorizationKey) {
        boolean exists = this.adminService.checkAdminExists(emailAddress, authorizationKey);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/findAll_admin")
    public ResponseEntity<List<AdminDto>> getAllAdmins() {
        List<Admin> admins = adminService.getAllAdmin();
        List<AdminDto> adminDtos = admins.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(adminDtos);
    }

    @GetMapping("/find_admin/{id}")
    public ResponseEntity<AdminDto> getAdmin(@PathVariable("id") String adminId) {
        Admin admin = this.adminService.getAdmin(adminId);
        return ResponseEntity.ok(toDto(admin));
    }

    @PostMapping("/login")
    public ResponseEntity<AdminDto> getAdmin(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("emailAddress");
        String password = credentials.get("password");
        Admin admin = adminService.getByEmailAddressAndPassword(email, password);
        return ResponseEntity.ok(toDto(admin));
    }

    @DeleteMapping("/delete_by_id/{id}")
    public ResponseEntity<Boolean> deleteAdminById(@PathVariable("id") String id) {
        return ResponseEntity.ok(this.adminService.deleteById(id));
    }

    @PutMapping("/update_admin/{adminId}")
    public ResponseEntity<AdminDto> updateAdmin(
            @RequestBody AdminDto adminDto,
            @PathVariable("adminId") String adminId) {
        Admin admin = toEntity(adminDto);
        Admin updatedAdmin = this.adminService.updateAdmin(admin, adminId);
        return ResponseEntity.ok(toDto(updatedAdmin));
    }

    @PatchMapping("/update_status/{adminId}")
    public ResponseEntity<AdminDto> updateAdminStatus(
            @PathVariable String adminId,
            @RequestBody Map<String, Object> body) {
        boolean status = (Boolean) body.get("status");
        Admin updatedAdmin = this.adminService.updateAdminStatus(adminId, status);
        return ResponseEntity.ok(toDto(updatedAdmin));
    }

    private AdminDto toDto(Admin admin) {
        AdminDto dto = new AdminDto();
        dto.setAdminId(admin.getAdminId());
        dto.setFullName(admin.getFullName());
        dto.setEmailAddress(admin.getEmailAddress());
        dto.setContactNumber(admin.getContactNumber());
        dto.setPositionAndRole(admin.getPositionAndRole());
        dto.setActive(admin.isActive());
        return dto;
    }

    private Admin toEntity(AdminRegistrationDto dto) {
        Admin admin = new Admin();
        // Don't set adminId here - let the service handle it so isNewAdmin check works
        admin.setFullName(dto.getFullName());
        admin.setEmailAddress(dto.getEmailAddress());
        admin.setPassword(dto.getPassword());
        admin.setContactNumber(dto.getContactNumber());
        admin.setPositionAndRole(dto.getPositionAndRole());
        admin.setAuthorizationKey(dto.getAuthorizationKey());
        return admin;
    }

    private Admin toEntity(AdminDto dto) {
        Admin admin = new Admin();
        admin.setAdminId(dto.getAdminId());
        admin.setFullName(dto.getFullName());
        admin.setEmailAddress(dto.getEmailAddress());
        admin.setContactNumber(dto.getContactNumber());
        admin.setPositionAndRole(dto.getPositionAndRole());
        admin.setActive(dto.isActive());
        return admin;
    }
}
