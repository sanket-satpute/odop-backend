package com.odop.root.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// PreAuthorize import removed - not used

@RestController
@RequestMapping("/test")
@CrossOrigin
public class TestController {

    @GetMapping("/ping")
    public ResponseEntity<String> testEndpoint() {
        System.out.println("DEBUG [Test Controller]: ping endpoint called");
        return ResponseEntity.ok("Hello World");
    }

    @GetMapping("/check-exists/{email}/{phone}")
    public ResponseEntity<String> alternativeCheckCustomerExists(
            @PathVariable("email") String email,
            @PathVariable("phone") long phone) {
        System.out.println("DEBUG [Test Controller]: alternative check endpoint called");
        System.out.println("DEBUG [Test Controller]: Email: " + email + ", Phone: " + phone);
        return ResponseEntity.ok("Alternative check endpoint reached");
    }
}
