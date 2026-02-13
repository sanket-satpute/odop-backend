package com.odop.root.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.odop.root.models.ContactUs;
import com.odop.root.services.ContactUsService;

@RestController
@RequestMapping("odop/contact")
@CrossOrigin
public class ContactUsController {

    @Autowired
    private ContactUsService contactUsService;

    // ============== CREATE ==============
    
    @PostMapping
    public ResponseEntity<ContactUs> saveMessage(@RequestBody ContactUs message) {
        message.setCreatedAt(LocalDateTime.now());
        message.setStatus("NEW");
        ContactUs saved = contactUsService.saveContact(message);
        return ResponseEntity.ok(saved);
    }

    // ============== READ ==============
    
    @GetMapping
    public ResponseEntity<List<ContactUs>> getAllMessages() {
        List<ContactUs> messages = contactUsService.getAllContactUs();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactUs> getMessageById(@PathVariable("id") String id) {
        Optional<ContactUs> message = contactUsService.getContactUsById(id);
        return message.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ContactUs>> getMessagesByStatus(@PathVariable("status") String status) {
        List<ContactUs> allMessages = contactUsService.getAllContactUs();
        List<ContactUs> filtered = allMessages.stream()
            .filter(m -> status.equalsIgnoreCase(m.getStatus()))
            .toList();
        return ResponseEntity.ok(filtered);
    }

    // ============== UPDATE ==============
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<ContactUs> updateStatus(
            @PathVariable("id") String id,
            @RequestBody StatusUpdateRequest request) {
        Optional<ContactUs> existing = contactUsService.getContactUsById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        ContactUs message = existing.get();
        message.setStatus(request.getStatus());
        message.setUpdatedAt(LocalDateTime.now());
        
        if (request.getAdminNotes() != null) {
            message.setAdminNotes(request.getAdminNotes());
        }
        
        ContactUs updated = contactUsService.saveContact(message);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/reply")
    public ResponseEntity<ContactUs> replyToMessage(
            @PathVariable("id") String id,
            @RequestBody ReplyRequest request) {
        Optional<ContactUs> existing = contactUsService.getContactUsById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        ContactUs message = existing.get();
        message.setReply(request.getReply());
        message.setRepliedAt(LocalDateTime.now());
        message.setRepliedBy(request.getRepliedBy());
        message.setStatus("REPLIED");
        message.setUpdatedAt(LocalDateTime.now());
        
        ContactUs updated = contactUsService.saveContact(message);
        return ResponseEntity.ok(updated);
    }

    // ============== DELETE ==============
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteMessage(@PathVariable("id") String id) {
        Optional<ContactUs> existing = contactUsService.getContactUsById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // For soft delete, just mark as deleted
        ContactUs message = existing.get();
        message.setStatus("DELETED");
        message.setUpdatedAt(LocalDateTime.now());
        contactUsService.saveContact(message);
        
        return ResponseEntity.ok(true);
    }

    // ============== DTOs ==============
    
    public static class StatusUpdateRequest {
        private String status;
        private String adminNotes;
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getAdminNotes() { return adminNotes; }
        public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    }
    
    public static class ReplyRequest {
        private String reply;
        private String repliedBy;
        
        public String getReply() { return reply; }
        public void setReply(String reply) { this.reply = reply; }
        public String getRepliedBy() { return repliedBy; }
        public void setRepliedBy(String repliedBy) { this.repliedBy = repliedBy; }
    }
}