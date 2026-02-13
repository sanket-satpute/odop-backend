package com.odop.service;

import com.odop.model.SupportTicket;
import com.odop.model.SupportTicket.TicketMessage;
import com.odop.repository.SupportTicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@Service
public class SupportTicketService {

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    // Customer Methods

    /**
     * Get all tickets for a customer
     */
    public List<SupportTicket> getCustomerTickets(String customerId) {
        return supportTicketRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    /**
     * Get paginated tickets for a customer
     */
    public Page<SupportTicket> getCustomerTickets(String customerId, Pageable pageable) {
        return supportTicketRepository.findByCustomerId(customerId, pageable);
    }

    /**
     * Get tickets by status for a customer
     */
    public List<SupportTicket> getCustomerTicketsByStatus(String customerId, String status) {
        return supportTicketRepository.findByCustomerIdAndStatus(customerId, status);
    }

    /**
     * Get open tickets for a customer
     */
    public List<SupportTicket> getOpenTickets(String customerId) {
        return supportTicketRepository.findOpenTicketsByCustomerId(customerId);
    }

    /**
     * Get ticket by ID
     */
    public Optional<SupportTicket> getTicketById(String ticketId) {
        return supportTicketRepository.findById(ticketId);
    }

    /**
     * Get ticket by ID for a specific customer (ownership check)
     */
    public Optional<SupportTicket> getTicketByIdForCustomer(String ticketId, String customerId) {
        Optional<SupportTicket> ticket = supportTicketRepository.findById(ticketId);
        if (ticket.isPresent() && ticket.get().getCustomerId().equals(customerId)) {
            return ticket;
        }
        return Optional.empty();
    }

    /**
     * Create a new support ticket
     */
    public SupportTicket createTicket(SupportTicket ticket) {
        ticket.setCreatedAt(new Date());
        ticket.setUpdatedAt(new Date());
        if (ticket.getStatus() == null) {
            ticket.setStatus("open");
        }
        if (ticket.getPriority() == null) {
            ticket.setPriority("medium");
        }
        return supportTicketRepository.save(ticket);
    }

    /**
     * Add a message to a ticket
     */
    public SupportTicket addMessage(String ticketId, String customerId, TicketMessage message) {
        Optional<SupportTicket> ticketOpt = supportTicketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            SupportTicket ticket = ticketOpt.get();
            // Verify ownership
            if (!ticket.getCustomerId().equals(customerId)) {
                throw new IllegalArgumentException("Ticket does not belong to this customer");
            }
            
            // If ticket was resolved/closed, reopen it
            if ("resolved".equals(ticket.getStatus()) || "closed".equals(ticket.getStatus())) {
                ticket.setStatus("open");
            }
            
            ticket.addMessage(message);
            ticket.setUpdatedAt(new Date());
            return supportTicketRepository.save(ticket);
        }
        throw new IllegalArgumentException("Ticket not found");
    }

    /**
     * Close a ticket by customer
     */
    public SupportTicket closeTicket(String ticketId, String customerId) {
        Optional<SupportTicket> ticketOpt = supportTicketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            SupportTicket ticket = ticketOpt.get();
            // Verify ownership
            if (!ticket.getCustomerId().equals(customerId)) {
                throw new IllegalArgumentException("Ticket does not belong to this customer");
            }
            ticket.setStatus("closed");
            ticket.setClosedAt(new Date());
            ticket.setUpdatedAt(new Date());
            return supportTicketRepository.save(ticket);
        }
        throw new IllegalArgumentException("Ticket not found");
    }

    /**
     * Search tickets for a customer
     */
    public List<SupportTicket> searchCustomerTickets(String customerId, String keyword) {
        return supportTicketRepository.searchByCustomerIdAndKeyword(customerId, keyword);
    }

    /**
     * Get ticket count for a customer
     */
    public long getTicketCount(String customerId) {
        return supportTicketRepository.countByCustomerId(customerId);
    }

    /**
     * Get ticket stats for a customer
     */
    public Map<String, Object> getCustomerTicketStats(String customerId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", supportTicketRepository.countByCustomerId(customerId));
        stats.put("open", supportTicketRepository.countByCustomerIdAndStatus(customerId, "open"));
        stats.put("inProgress", supportTicketRepository.countByCustomerIdAndStatus(customerId, "in-progress"));
        stats.put("resolved", supportTicketRepository.countByCustomerIdAndStatus(customerId, "resolved"));
        stats.put("closed", supportTicketRepository.countByCustomerIdAndStatus(customerId, "closed"));
        return stats;
    }

    /**
     * Mark messages as read for a customer
     */
    public SupportTicket markMessagesAsRead(String ticketId, String customerId) {
        Optional<SupportTicket> ticketOpt = supportTicketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            SupportTicket ticket = ticketOpt.get();
            // Verify ownership
            if (!ticket.getCustomerId().equals(customerId)) {
                throw new IllegalArgumentException("Ticket does not belong to this customer");
            }
            
            // Mark all messages from support as read
            if (ticket.getMessages() != null) {
                for (TicketMessage msg : ticket.getMessages()) {
                    if (!msg.getSenderId().equals(customerId)) {
                        msg.setRead(true);
                    }
                }
            }
            ticket.setUpdatedAt(new Date());
            return supportTicketRepository.save(ticket);
        }
        throw new IllegalArgumentException("Ticket not found");
    }

    // Admin/Support Methods

    /**
     * Get all tickets (admin)
     */
    public List<SupportTicket> getAllTickets() {
        return supportTicketRepository.findAll();
    }

    /**
     * Get all tickets paginated (admin)
     */
    public Page<SupportTicket> getAllTickets(Pageable pageable) {
        return supportTicketRepository.findAll(pageable);
    }

    /**
     * Get tickets by status (admin)
     */
    public List<SupportTicket> getTicketsByStatus(String status) {
        return supportTicketRepository.findByStatus(status);
    }

    /**
     * Get all open tickets (admin)
     */
    public List<SupportTicket> getAllOpenTickets() {
        return supportTicketRepository.findAllOpenTickets();
    }

    /**
     * Get unassigned tickets (admin)
     */
    public List<SupportTicket> getUnassignedTickets() {
        return supportTicketRepository.findUnassignedTickets();
    }

    /**
     * Assign ticket to support agent (admin)
     */
    public SupportTicket assignTicket(String ticketId, String assignedTo) {
        Optional<SupportTicket> ticketOpt = supportTicketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            SupportTicket ticket = ticketOpt.get();
            ticket.setAssignedTo(assignedTo);
            if ("open".equals(ticket.getStatus())) {
                ticket.setStatus("in-progress");
            }
            ticket.setUpdatedAt(new Date());
            return supportTicketRepository.save(ticket);
        }
        throw new IllegalArgumentException("Ticket not found");
    }

    /**
     * Update ticket status (admin/support)
     */
    public SupportTicket updateTicketStatus(String ticketId, String status) {
        Optional<SupportTicket> ticketOpt = supportTicketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            SupportTicket ticket = ticketOpt.get();
            ticket.setStatus(status);
            
            if ("resolved".equals(status)) {
                ticket.setResolvedAt(new Date());
            } else if ("closed".equals(status)) {
                ticket.setClosedAt(new Date());
            }
            
            ticket.setUpdatedAt(new Date());
            return supportTicketRepository.save(ticket);
        }
        throw new IllegalArgumentException("Ticket not found");
    }

    /**
     * Update ticket priority (admin/support)
     */
    public SupportTicket updateTicketPriority(String ticketId, String priority) {
        Optional<SupportTicket> ticketOpt = supportTicketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            SupportTicket ticket = ticketOpt.get();
            ticket.setPriority(priority);
            ticket.setUpdatedAt(new Date());
            return supportTicketRepository.save(ticket);
        }
        throw new IllegalArgumentException("Ticket not found");
    }

    /**
     * Add resolution and close ticket (admin/support)
     */
    public SupportTicket resolveTicket(String ticketId, String resolution) {
        Optional<SupportTicket> ticketOpt = supportTicketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            SupportTicket ticket = ticketOpt.get();
            ticket.setResolution(resolution);
            ticket.setStatus("resolved");
            ticket.setResolvedAt(new Date());
            ticket.setUpdatedAt(new Date());
            return supportTicketRepository.save(ticket);
        }
        throw new IllegalArgumentException("Ticket not found");
    }

    /**
     * Add message from support (admin/support)
     */
    public SupportTicket addSupportMessage(String ticketId, TicketMessage message) {
        Optional<SupportTicket> ticketOpt = supportTicketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            SupportTicket ticket = ticketOpt.get();
            ticket.addMessage(message);
            ticket.setUpdatedAt(new Date());
            return supportTicketRepository.save(ticket);
        }
        throw new IllegalArgumentException("Ticket not found");
    }

    /**
     * Search all tickets (admin)
     */
    public List<SupportTicket> searchTickets(String keyword) {
        return supportTicketRepository.searchByKeyword(keyword);
    }

    /**
     * Get tickets assigned to a support agent
     */
    public List<SupportTicket> getTicketsAssignedTo(String agentId) {
        return supportTicketRepository.findByAssignedTo(agentId);
    }

    /**
     * Get global ticket stats (admin)
     */
    public Map<String, Object> getGlobalTicketStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", supportTicketRepository.count());
        stats.put("open", supportTicketRepository.countByStatus("open"));
        stats.put("inProgress", supportTicketRepository.countByStatus("in-progress"));
        stats.put("resolved", supportTicketRepository.countByStatus("resolved"));
        stats.put("closed", supportTicketRepository.countByStatus("closed"));
        stats.put("lowPriority", supportTicketRepository.countByPriority("low"));
        stats.put("mediumPriority", supportTicketRepository.countByPriority("medium"));
        stats.put("highPriority", supportTicketRepository.countByPriority("high"));
        stats.put("urgentPriority", supportTicketRepository.countByPriority("urgent"));
        return stats;
    }
}
