package com.odop.repository;

import com.odop.model.SupportTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SupportTicketRepository extends MongoRepository<SupportTicket, String> {

    // Find tickets by customer ID
    List<SupportTicket> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    Page<SupportTicket> findByCustomerId(String customerId, Pageable pageable);

    // Find tickets by status
    List<SupportTicket> findByCustomerIdAndStatus(String customerId, String status);

    List<SupportTicket> findByStatus(String status);

    Page<SupportTicket> findByStatus(String status, Pageable pageable);

    // Find tickets by category
    List<SupportTicket> findByCustomerIdAndCategory(String customerId, String category);

    List<SupportTicket> findByCategory(String category);

    // Find tickets by priority
    List<SupportTicket> findByPriority(String priority);

    List<SupportTicket> findByPriorityAndStatus(String priority, String status);

    // Find tickets assigned to a support agent
    List<SupportTicket> findByAssignedTo(String assignedTo);

    List<SupportTicket> findByAssignedToAndStatus(String assignedTo, String status);

    // Find tickets by order ID
    List<SupportTicket> findByOrderId(String orderId);

    // Count methods
    long countByCustomerId(String customerId);

    long countByCustomerIdAndStatus(String customerId, String status);

    long countByStatus(String status);

    long countByPriority(String priority);

    long countByAssignedTo(String assignedTo);

    // Find open tickets (not resolved or closed)
    @Query("{ 'customerId': ?0, 'status': { $nin: ['resolved', 'closed'] } }")
    List<SupportTicket> findOpenTicketsByCustomerId(String customerId);

    @Query("{ 'status': { $nin: ['resolved', 'closed'] } }")
    List<SupportTicket> findAllOpenTickets();

    // Find tickets created within a date range
    @Query("{ 'createdAt': { $gte: ?0, $lte: ?1 } }")
    List<SupportTicket> findByCreatedAtBetween(Date startDate, Date endDate);

    // Search tickets by subject or description
    @Query("{ 'customerId': ?0, $or: [ { 'subject': { $regex: ?1, $options: 'i' } }, { 'description': { $regex: ?1, $options: 'i' } } ] }")
    List<SupportTicket> searchByCustomerIdAndKeyword(String customerId, String keyword);

    @Query("{ $or: [ { 'subject': { $regex: ?0, $options: 'i' } }, { 'description': { $regex: ?0, $options: 'i' } }, { 'ticketId': { $regex: ?0, $options: 'i' } } ] }")
    List<SupportTicket> searchByKeyword(String keyword);

    // Find unassigned tickets
    @Query("{ 'assignedTo': null, 'status': { $nin: ['resolved', 'closed'] } }")
    List<SupportTicket> findUnassignedTickets();

    // Find tickets with unread messages for a user
    @Query("{ 'customerId': ?0, 'messages': { $elemMatch: { 'isRead': false, 'senderId': { $ne: ?0 } } } }")
    List<SupportTicket> findTicketsWithUnreadMessages(String customerId);
}
