package com.odop.root.models;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * ContactUs entity for customer inquiries and support requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "contactus")
public class ContactUs {

    @Id
    private String contactId;
    private String fullName;
    private String emailAddress;
    private String subject;
    private String message;
    @Builder.Default
    private String status = "NEW"; // NEW, IN_PROGRESS, REPLIED, RESOLVED, DELETED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Admin response fields
    private String reply;
    private String repliedBy;
    private LocalDateTime repliedAt;
    private String adminNotes;

    /**
     * Convenience constructor for new contact submissions.
     */
    public ContactUs(String fullName, String emailAddress, String subject, String message) {
        this.fullName = fullName;
        this.emailAddress = emailAddress;
        this.subject = subject;
        this.message = message;
        this.status = "NEW";
    }
}
