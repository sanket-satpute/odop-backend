package com.odop.root.invoice.controller;

import com.odop.root.invoice.dto.InvoiceDto;
import com.odop.root.invoice.model.Invoice;
import com.odop.root.invoice.service.InvoiceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for invoice operations.
 */
@RestController
@RequestMapping("/odop/invoice")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:63699"})
public class InvoiceController {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);

    @Autowired
    private InvoiceService invoiceService;

    /**
     * Generate invoice for an order
     */
    @PostMapping("/generate/{orderId}")
    public ResponseEntity<InvoiceDto> generateInvoice(@PathVariable String orderId) {
        logger.info("Generating invoice for order: {}", orderId);
        try {
            Invoice invoice = invoiceService.generateInvoice(orderId);
            return ResponseEntity.ok(invoiceService.toDto(invoice));
        } catch (Exception e) {
            logger.error("Error generating invoice: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get invoice by order ID
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<InvoiceDto> getInvoiceByOrder(@PathVariable String orderId) {
        Invoice invoice = invoiceService.getInvoiceByOrderId(orderId);
        if (invoice == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(invoiceService.toDto(invoice));
    }

    /**
     * Download invoice PDF
     */
    @GetMapping("/download/{invoiceId}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable String invoiceId) {
        logger.info("Downloading invoice PDF: {}", invoiceId);
        try {
            byte[] pdfBytes = invoiceService.generateInvoicePdf(invoiceId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment()
                .filename("invoice-" + invoiceId + ".pdf")
                .build());
            headers.setContentLength(pdfBytes.length);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error downloading invoice: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Download invoice PDF by order ID
     */
    @GetMapping("/download/order/{orderId}")
    public ResponseEntity<byte[]> downloadInvoiceByOrder(@PathVariable String orderId) {
        logger.info("Downloading invoice PDF for order: {}", orderId);
        try {
            byte[] pdfBytes = invoiceService.generateInvoicePdfForOrder(orderId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment()
                .filename("invoice-order-" + orderId + ".pdf")
                .build());
            headers.setContentLength(pdfBytes.length);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error downloading invoice: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Preview invoice as HTML (for web view)
     */
    @GetMapping("/preview/{invoiceId}")
    public ResponseEntity<InvoiceDto> previewInvoice(@PathVariable String invoiceId) {
        try {
            // For now, return the DTO. Frontend can render it.
            Invoice invoice = invoiceService.getInvoiceByOrderId(invoiceId);
            if (invoice == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(invoiceService.toDto(invoice));
        } catch (Exception e) {
            logger.error("Error previewing invoice: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all invoices for a customer
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<InvoiceDto>> getCustomerInvoices(@PathVariable String customerId) {
        List<Invoice> invoices = invoiceService.getCustomerInvoices(customerId);
        List<InvoiceDto> dtos = invoices.stream()
            .map(invoiceService::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get all invoices for a vendor
     */
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<InvoiceDto>> getVendorInvoices(@PathVariable String vendorId) {
        List<Invoice> invoices = invoiceService.getVendorInvoices(vendorId);
        List<InvoiceDto> dtos = invoices.stream()
            .map(invoiceService::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
