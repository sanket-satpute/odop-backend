package com.odop.controller;

import com.odop.model.SupportTicket;
import com.odop.model.SupportTicket.TicketMessage;
import com.odop.service.SupportTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/odop/customer/{customerId}/support")
@CrossOrigin(origins = "*")
public class SupportTicketController {

    @Autowired
    private SupportTicketService supportTicketService;

    /**
     * Get all tickets for a customer
     */
    @GetMapping("/tickets")
    public ResponseEntity<List<SupportTicket>> getCustomerTickets(
            @PathVariable String customerId,
            @RequestParam(required = false) String status) {
        
        List<SupportTicket> tickets;
        if (status != null && !status.isEmpty()) {
            tickets = supportTicketService.getCustomerTicketsByStatus(customerId, status);
        } else {
            tickets = supportTicketService.getCustomerTickets(customerId);
        }
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get paginated tickets for a customer
     */
    @GetMapping("/tickets/page")
    public ResponseEntity<Page<SupportTicket>> getCustomerTicketsPaginated(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SupportTicket> tickets = supportTicketService.getCustomerTickets(customerId, pageRequest);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get open tickets for a customer
     */
    @GetMapping("/tickets/open")
    public ResponseEntity<List<SupportTicket>> getOpenTickets(@PathVariable String customerId) {
        List<SupportTicket> tickets = supportTicketService.getOpenTickets(customerId);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get a specific ticket by ID
     */
    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<?> getTicketById(
            @PathVariable String customerId,
            @PathVariable String ticketId) {
        
        Optional<SupportTicket> ticket = supportTicketService.getTicketByIdForCustomer(ticketId, customerId);
        if (ticket.isPresent()) {
            return ResponseEntity.ok(ticket.get());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Create a new support ticket
     */
    @PostMapping("/tickets")
    public ResponseEntity<SupportTicket> createTicket(
            @PathVariable String customerId,
            @RequestBody SupportTicket ticket) {
        
        ticket.setCustomerId(customerId);
        SupportTicket createdTicket = supportTicketService.createTicket(ticket);
        return ResponseEntity.ok(createdTicket);
    }

    /**
     * Add a message to a ticket
     */
    @PostMapping("/tickets/{ticketId}/messages")
    public ResponseEntity<?> addMessage(
            @PathVariable String customerId,
            @PathVariable String ticketId,
            @RequestBody TicketMessage message) {
        
        try {
            message.setSenderId(customerId);
            message.setSenderRole("customer");
            message.setTimestamp(new Date());
            
            SupportTicket updatedTicket = supportTicketService.addMessage(ticketId, customerId, message);
            return ResponseEntity.ok(updatedTicket);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Close a ticket
     */
    @PatchMapping("/tickets/{ticketId}/close")
    public ResponseEntity<?> closeTicket(
            @PathVariable String customerId,
            @PathVariable String ticketId) {
        
        try {
            SupportTicket ticket = supportTicketService.closeTicket(ticketId, customerId);
            return ResponseEntity.ok(ticket);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Mark messages as read
     */
    @PatchMapping("/tickets/{ticketId}/read")
    public ResponseEntity<?> markMessagesAsRead(
            @PathVariable String customerId,
            @PathVariable String ticketId) {
        
        try {
            SupportTicket ticket = supportTicketService.markMessagesAsRead(ticketId, customerId);
            return ResponseEntity.ok(ticket);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Search tickets
     */
    @GetMapping("/tickets/search")
    public ResponseEntity<List<SupportTicket>> searchTickets(
            @PathVariable String customerId,
            @RequestParam String keyword) {
        
        List<SupportTicket> tickets = supportTicketService.searchCustomerTickets(customerId, keyword);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get ticket statistics for a customer
     */
    @GetMapping("/tickets/stats")
    public ResponseEntity<Map<String, Object>> getTicketStats(@PathVariable String customerId) {
        Map<String, Object> stats = supportTicketService.getCustomerTicketStats(customerId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get ticket count for a customer
     */
    @GetMapping("/tickets/count")
    public ResponseEntity<Map<String, Long>> getTicketCount(@PathVariable String customerId) {
        long count = supportTicketService.getTicketCount(customerId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
