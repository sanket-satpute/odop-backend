package com.odop.root.invoice.service;

import com.odop.root.invoice.dto.InvoiceDto;
import com.odop.root.invoice.model.Invoice;
import com.odop.root.invoice.model.Invoice.InvoiceItem;
import com.odop.root.invoice.repository.InvoiceRepository;
import com.odop.root.models.*;
import com.odop.root.repository.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating and managing invoices.
 */
@Service
public class InvoiceService {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${company.name:ODOP - One District One Product}")
    private String companyName;

    @Value("${company.address:Government of India Initiative}")
    private String companyAddress;

    @Value("${company.gstin:}")
    private String companyGstin;

    // GST Rates (can be configured)
    private static final double DEFAULT_GST_RATE = 18.0;  // 18% GST
    private static final double CGST_RATE = 9.0;          // 9% CGST
    private static final double SGST_RATE = 9.0;          // 9% SGST
    private static final double IGST_RATE = 18.0;         // 18% IGST (inter-state)

    /**
     * Generate invoice for an order
     */
    public Invoice generateInvoice(String orderId) {
        logger.info("Generating invoice for order: {}", orderId);

        // Check if invoice already exists
        Optional<Invoice> existingInvoice = invoiceRepository.findByOrderId(orderId);
        if (existingInvoice.isPresent()) {
            logger.info("Invoice already exists for order: {}", orderId);
            return existingInvoice.get();
        }

        // Get order details
        Order order = orderRepository.findByOrderId(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found: " + orderId);
        }

        // Get customer details
        Customer customer = customerRepository.findById(order.getCustomerId()).orElse(null);
        
        // Get vendor details
        Vendor vendor = null;
        if (order.getVendorId() != null) {
            vendor = vendorRepository.findById(order.getVendorId()).orElse(null);
        }

        // Determine supply type (inter-state or intra-state)
        boolean isInterState = isInterStateSupply(customer, vendor);

        // Build invoice
        Invoice invoice = Invoice.builder()
            .invoiceNumber(generateInvoiceNumber())
            .orderId(orderId)
            .invoiceDate(LocalDateTime.now())
            .dueDate(LocalDateTime.now().plusDays(30))
            .status("GENERATED")
            .build();

        // Set customer details
        if (customer != null) {
            invoice.setCustomerId(customer.getCustomerId());
            invoice.setCustomerName(customer.getFullName());
            invoice.setCustomerEmail(customer.getEmailAddress());
            invoice.setCustomerPhone(String.valueOf(customer.getContactNumber()));
            invoice.setCustomerAddress(customer.getAddress());
            invoice.setCustomerCity(customer.getCity());
            invoice.setCustomerState(customer.getState());
            invoice.setCustomerPinCode(customer.getPinCode());
        } else {
            // Use shipping address from order
            invoice.setCustomerAddress(order.getShippingAddress());
            invoice.setCustomerState(order.getShippingState());
            invoice.setCustomerPinCode(order.getShippingPinCode());
        }

        // Set vendor details
        if (vendor != null) {
            invoice.setVendorId(vendor.getVendorId());
            invoice.setVendorName(vendor.getShopkeeperName());
            invoice.setVendorShopName(vendor.getShoppeeName());
            invoice.setVendorAddress(vendor.getShoppeeAddress());
            invoice.setVendorCity(vendor.getLocationDistrict());
            invoice.setVendorState(vendor.getLocationState());
            invoice.setVendorPinCode(vendor.getPinCode());
            invoice.setVendorGstin(vendor.getTaxIdentificationNumber());
        }

        // Set company details
        invoice.setCompanyName(companyName);
        invoice.setCompanyAddress(companyAddress);
        invoice.setCompanyGstin(companyGstin);

        // Convert order items to invoice items with GST calculation
        List<InvoiceItem> invoiceItems = new ArrayList<>();
        double subtotal = 0;

        if (order.getOrderItems() != null) {
            for (OrderItem orderItem : order.getOrderItems()) {
                Products product = productRepository.findById(orderItem.getProductId()).orElse(null);
                
                InvoiceItem item = InvoiceItem.builder()
                    .productId(orderItem.getProductId())
                    .productName(orderItem.getProductName())
                    .description(product != null ? truncate(product.getProductDescription(), 100) : "")
                    .quantity(orderItem.getQuantity())
                    .unitPrice(orderItem.getUnitPrice())
                    .discount(0.0) // Can be calculated from order discount
                    .build();

                // Calculate taxable value
                double taxableValue = orderItem.getUnitPrice() * orderItem.getQuantity();
                item.setTaxableValue(taxableValue);

                // Calculate GST based on supply type
                if (isInterState) {
                    item.setIgstRate(IGST_RATE);
                    item.setIgstAmount(taxableValue * IGST_RATE / 100);
                    item.setCgstRate(0.0);
                    item.setCgstAmount(0.0);
                    item.setSgstRate(0.0);
                    item.setSgstAmount(0.0);
                } else {
                    item.setCgstRate(CGST_RATE);
                    item.setCgstAmount(taxableValue * CGST_RATE / 100);
                    item.setSgstRate(SGST_RATE);
                    item.setSgstAmount(taxableValue * SGST_RATE / 100);
                    item.setIgstRate(0.0);
                    item.setIgstAmount(0.0);
                }

                // Calculate total
                double totalTax = item.getCgstAmount() + item.getSgstAmount() + item.getIgstAmount();
                item.setTotalAmount(taxableValue + totalTax);

                invoiceItems.add(item);
                subtotal += taxableValue;
            }
        }

        invoice.setItems(invoiceItems);
        invoice.setSubtotal(subtotal);
        invoice.setDiscountAmount(order.getDiscountAmount());
        invoice.setTaxableAmount(subtotal - order.getDiscountAmount());

        // Calculate total GST
        if (isInterState) {
            invoice.setIgstRate(IGST_RATE);
            invoice.setIgstAmount(invoice.getTaxableAmount() * IGST_RATE / 100);
            invoice.setCgstRate(0.0);
            invoice.setCgstAmount(0.0);
            invoice.setSgstRate(0.0);
            invoice.setSgstAmount(0.0);
            invoice.setSupplyType("INTER_STATE");
        } else {
            invoice.setCgstRate(CGST_RATE);
            invoice.setCgstAmount(invoice.getTaxableAmount() * CGST_RATE / 100);
            invoice.setSgstRate(SGST_RATE);
            invoice.setSgstAmount(invoice.getTaxableAmount() * SGST_RATE / 100);
            invoice.setIgstRate(0.0);
            invoice.setIgstAmount(0.0);
            invoice.setSupplyType("INTRA_STATE");
        }

        invoice.setTotalGst(invoice.getCgstAmount() + invoice.getSgstAmount() + invoice.getIgstAmount());
        invoice.setShippingCharges(order.getDeliveryCharges());
        invoice.setGrandTotal(invoice.getTaxableAmount() + invoice.getTotalGst() + invoice.getShippingCharges());

        // Payment info
        invoice.setPaymentMethod(order.getPaymentMethod());
        invoice.setPaymentStatus(order.getPaymentStatus());
        invoice.setTransactionId(order.getPaymentTransactionId());
        invoice.setPlaceOfSupply(order.getShippingState());

        // Set standard terms
        invoice.setTermsAndConditions(getDefaultTermsAndConditions());

        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());

        // Save and return
        Invoice savedInvoice = invoiceRepository.save(invoice);
        logger.info("Invoice generated: {}", savedInvoice.getInvoiceNumber());

        return savedInvoice;
    }

    /**
     * Generate PDF bytes for an invoice
     */
    public byte[] generateInvoicePdf(String invoiceId) {
        logger.info("Generating PDF for invoice: {}", invoiceId);

        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));

        try {
            // Generate HTML from Thymeleaf template
            String html = generateInvoiceHtml(invoice);

            // Convert HTML to PDF using Flying Saucer
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);

            logger.info("PDF generated successfully for invoice: {}", invoice.getInvoiceNumber());
            return outputStream.toByteArray();

        } catch (Exception e) {
            logger.error("Error generating PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage());
        }
    }

    /**
     * Generate PDF for an order (auto-generates invoice if not exists)
     */
    public byte[] generateInvoicePdfForOrder(String orderId) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId)
            .orElseGet(() -> generateInvoice(orderId));
        
        return generateInvoicePdf(invoice.getInvoiceId());
    }

    /**
     * Get invoice by order ID
     */
    public Invoice getInvoiceByOrderId(String orderId) {
        return invoiceRepository.findByOrderId(orderId).orElse(null);
    }

    /**
     * Get invoices for a customer
     */
    public List<Invoice> getCustomerInvoices(String customerId) {
        return invoiceRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    /**
     * Get invoices for a vendor
     */
    public List<Invoice> getVendorInvoices(String vendorId) {
        return invoiceRepository.findByVendorIdOrderByCreatedAtDesc(vendorId);
    }

    /**
     * Convert Invoice to DTO
     */
    public InvoiceDto toDto(Invoice invoice) {
        if (invoice == null) return null;

        List<InvoiceDto.InvoiceItemDto> itemDtos = new ArrayList<>();
        if (invoice.getItems() != null) {
            itemDtos = invoice.getItems().stream()
                .map(item -> InvoiceDto.InvoiceItemDto.builder()
                    .productName(item.getProductName())
                    .description(item.getDescription())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .discount(item.getDiscount())
                    .totalAmount(item.getTotalAmount())
                    .build())
                .collect(Collectors.toList());
        }

        return InvoiceDto.builder()
            .invoiceId(invoice.getInvoiceId())
            .invoiceNumber(invoice.getInvoiceNumber())
            .orderId(invoice.getOrderId())
            .customerName(invoice.getCustomerName())
            .customerEmail(invoice.getCustomerEmail())
            .customerPhone(invoice.getCustomerPhone())
            .customerAddress(formatAddress(invoice))
            .vendorName(invoice.getVendorName())
            .vendorShopName(invoice.getVendorShopName())
            .items(itemDtos)
            .subtotal(invoice.getSubtotal())
            .discountAmount(invoice.getDiscountAmount())
            .taxAmount(invoice.getTotalGst())
            .shippingCharges(invoice.getShippingCharges())
            .grandTotal(invoice.getGrandTotal())
            .status(invoice.getStatus())
            .paymentStatus(invoice.getPaymentStatus())
            .invoiceDate(invoice.getInvoiceDate())
            .dueDate(invoice.getDueDate())
            .pdfUrl(invoice.getPdfUrl())
            .pdfAvailable(true)
            .build();
    }

    // ================ PRIVATE HELPER METHODS ================

    private String generateInvoiceNumber() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        long count = invoiceRepository.count() + 1;
        return String.format("ODOP-INV-%s-%05d", year, count);
    }

    private boolean isInterStateSupply(Customer customer, Vendor vendor) {
        if (customer == null || vendor == null) return false;
        String customerState = customer.getState();
        String vendorState = vendor.getLocationState();
        
        if (customerState == null || vendorState == null) return false;
        return !customerState.equalsIgnoreCase(vendorState);
    }

    private String generateInvoiceHtml(Invoice invoice) {
        Context context = new Context();
        context.setVariable("invoice", invoice);
        context.setVariable("formattedDate", invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        context.setVariable("formattedDueDate", invoice.getDueDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        context.setVariable("amountInWords", convertToWords(invoice.getGrandTotal()));
        
        return templateEngine.process("invoice-template", context);
    }

    private String formatAddress(Invoice invoice) {
        StringBuilder sb = new StringBuilder();
        if (invoice.getCustomerAddress() != null) sb.append(invoice.getCustomerAddress());
        if (invoice.getCustomerCity() != null) sb.append(", ").append(invoice.getCustomerCity());
        if (invoice.getCustomerState() != null) sb.append(", ").append(invoice.getCustomerState());
        if (invoice.getCustomerPinCode() != null) sb.append(" - ").append(invoice.getCustomerPinCode());
        return sb.toString();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    private String getDefaultTermsAndConditions() {
        return "1. Payment is due within 30 days.\n" +
               "2. Products are covered under vendor's return policy.\n" +
               "3. For any disputes, please contact support@odop.gov.in\n" +
               "4. This is a computer-generated invoice.";
    }

    /**
     * Convert number to words (Indian format)
     */
    private String convertToWords(Double amount) {
        if (amount == null) return "Zero Rupees Only";
        
        long rupees = amount.longValue();
        int paise = (int) Math.round((amount - rupees) * 100);
        
        String rupeesInWords = numberToWords(rupees);
        
        if (paise > 0) {
            return rupeesInWords + " Rupees and " + numberToWords(paise) + " Paise Only";
        }
        return rupeesInWords + " Rupees Only";
    }

    private String numberToWords(long number) {
        if (number == 0) return "Zero";
        
        String[] ones = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", 
                        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", 
                        "Seventeen", "Eighteen", "Nineteen"};
        String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};
        
        if (number < 20) return ones[(int) number];
        if (number < 100) return tens[(int) number / 10] + (number % 10 > 0 ? " " + ones[(int) number % 10] : "");
        if (number < 1000) return ones[(int) number / 100] + " Hundred" + (number % 100 > 0 ? " " + numberToWords(number % 100) : "");
        if (number < 100000) return numberToWords(number / 1000) + " Thousand" + (number % 1000 > 0 ? " " + numberToWords(number % 1000) : "");
        if (number < 10000000) return numberToWords(number / 100000) + " Lakh" + (number % 100000 > 0 ? " " + numberToWords(number % 100000) : "");
        return numberToWords(number / 10000000) + " Crore" + (number % 10000000 > 0 ? " " + numberToWords(number % 10000000) : "");
    }
}
