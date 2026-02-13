package com.odop.root.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.odop.root.dto.CustomerDto;
import com.odop.root.dto.CustomerRegistrationDto;
import com.odop.root.models.Customer;
import com.odop.root.services.CustomerService;

@RestController
@RequestMapping("odop/customer")
@CrossOrigin
public class CustomerController {

    @Autowired
    private CustomerService customerService;
    private static final Logger logger = LogManager.getLogger(CustomerController.class);

    @PostMapping("/create_account")
    public ResponseEntity<CustomerDto> createCustomerAccount(@RequestBody CustomerRegistrationDto registrationDto) {
        Customer customer = toEntity(registrationDto);
        Customer savedCustomer = customerService.saveCustomer(customer);
        return ResponseEntity.ok(toDto(savedCustomer));
    }

    @GetMapping("/get_all_customers")
    public ResponseEntity<List<CustomerDto>> getAllCustomers() {
        List<Customer> customers = this.customerService.getAllCustomer();
        List<CustomerDto> customerDtos = customers.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(customerDtos);
    }

    @GetMapping("/get_customer_id/{id}")
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable("id") String uid) {
        Customer customer = this.customerService.getCustomerById(uid);
        return ResponseEntity.ok(toDto(customer));
    }

    @PostMapping("/login")
    public ResponseEntity<CustomerDto> getCustomerByEmailAndPassword(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("emailAddress");
        String password = credentials.get("password");
        Customer customer = this.customerService.getCustomerByEmailAndPassword(email, password);
        return ResponseEntity.ok(toDto(customer));
    }

    @DeleteMapping("/delete_by_id/{id}")
    public ResponseEntity<Boolean> deleteCustomerById(@PathVariable("id") String id) {
        return ResponseEntity.ok(this.customerService.deleteById(id));
    }

    @PutMapping("/update_customer_by_id/{id}")
    public ResponseEntity<CustomerDto> updateCustomerById(
            @PathVariable("id") String id,
            @RequestBody CustomerDto customerDto) {
        Customer customer = toEntity(customerDto);
        Customer updatedCustomer = customerService.saveCustomer(customer);
        return ResponseEntity.ok(toDto(updatedCustomer));
    }

    @GetMapping("/check_customer_exists/{email}/{phone}")
    @PreAuthorize("permitAll")
    public ResponseEntity<Boolean> checkIfCustomerExists(
            @PathVariable("email") String email,
            @PathVariable("phone") long phone) {
        System.out.println("DEBUG [Controller]: checkIfCustomerExists called");
        System.out.println("DEBUG [Controller]: Email: " + email + ", Phone: " + phone);

        boolean exists = this.customerService.existsByEmailAddressOrContactNumber(email, phone);
        System.out.println("DEBUG [Controller]: Service returned exists: " + exists);

        ResponseEntity<Boolean> response = ResponseEntity.ok(exists);
        System.out.println("DEBUG [Controller]: Returning response: " + response.getStatusCode());
        return response;
    }

    @PatchMapping("/update_status/{customerId}")
    public ResponseEntity<CustomerDto> updateCustomerStatus(
            @PathVariable String customerId,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        Customer updatedCustomer = this.customerService.updateCustomerStatus(customerId, status);
        return ResponseEntity.ok(toDto(updatedCustomer));
    }

    private CustomerDto toDto(Customer customer) {
        CustomerDto dto = new CustomerDto();
        dto.setCustomerId(customer.getCustomerId());
        dto.setFullName(customer.getFullName());
        dto.setEmailAddress(customer.getEmailAddress());
        dto.setContactNumber(customer.getContactNumber());
        dto.setAddress(customer.getAddress());
        dto.setCity(customer.getCity());
        dto.setState(customer.getState());
        dto.setPinCode(customer.getPinCode());
        dto.setStatus(customer.getStatus());
        return dto;
    }

    private Customer toEntity(CustomerRegistrationDto dto) {
        Customer customer = new Customer();
        customer.setFullName(dto.getFullName());
        customer.setEmailAddress(dto.getEmailAddress());
        customer.setPassword(dto.getPassword());
        customer.setContactNumber(dto.getContactNumber());
        customer.setAddress(dto.getAddress());
        customer.setCity(dto.getCity());
        customer.setState(dto.getState());
        customer.setPinCode(dto.getPinCode());
        return customer;
    }

    private Customer toEntity(CustomerDto dto) {
        Customer customer = new Customer();
        customer.setCustomerId(dto.getCustomerId());
        customer.setFullName(dto.getFullName());
        customer.setEmailAddress(dto.getEmailAddress());
        customer.setContactNumber(dto.getContactNumber());
        customer.setAddress(dto.getAddress());
        customer.setCity(dto.getCity());
        customer.setState(dto.getState());
        customer.setPinCode(dto.getPinCode());
        customer.setStatus(dto.getStatus());
        return customer;
    }
}
