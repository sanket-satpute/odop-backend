# 🚀 ODOP Medium-Priority Features - Implementation Plan

## Overview

This document outlines the implementation plan for 6 medium-priority features for the ODOP e-commerce platform.

---

## 📋 Feature Summary

| # | Feature | Complexity | Est. Time | Dependencies | Status |
|---|---------|------------|-----------|--------------|--------|
| 1 | Social Login | Medium | 4-5 hours | OAuth2, Spring Security | 🔄 Pending |
| 2 | Product Variants | High | 6-8 hours | Product module | 🔄 Pending |
| 3 | Bulk Upload (CSV) | Medium | 4-5 hours | Product module | 🔄 Pending |
| 4 | Report Generation | High | 6-8 hours | Order, Payment modules | 🔄 Pending |
| 5 | Chat Support | High | 8-10 hours | WebSocket | 🔄 Pending |
| 6 | Return/Refund Module | High | 6-8 hours | Order, Payment modules | 🔄 Pending |

---

# Feature 1: Social Login (Google/Facebook)

## 🎯 Goal
Allow users to sign in using Google or Facebook accounts for seamless authentication.

## 📝 Implementation Steps

### Backend Tasks
1. **Add OAuth2 dependencies** (spring-boot-starter-oauth2-client)
2. **Configure OAuth2 providers** in application.properties
3. **Create OAuth2 User Service** to handle social login
4. **Create Social Auth Controller** for token exchange
5. **Update User model** with social provider info
6. **Handle account linking** (social + existing email)

### Frontend Tasks
1. **Add Google/Facebook SDK scripts**
2. **Create social login buttons**
3. **Handle OAuth callback**
4. **Store tokens and user info**

## ⚠️ Potential Problems & Solutions

| Problem | Solution |
|---------|----------|
| User exists with same email | Prompt to link accounts or auto-link |
| Token expiration | Implement refresh token flow |
| Missing required fields (phone) | Prompt for additional info after social login |
| CORS issues with OAuth redirect | Configure proper redirect URIs |

## 📁 Files to Create/Modify

### Backend
- `pom.xml` - Add OAuth2 dependencies
- `oauth2/config/OAuth2Config.java` - OAuth2 configuration
- `oauth2/service/CustomOAuth2UserService.java` - Handle OAuth2 user
- `oauth2/controller/SocialAuthController.java` - REST endpoints
- `oauth2/dto/SocialLoginRequest.java` - Request DTO
- `oauth2/dto/SocialUserInfo.java` - Social user info
- `application.properties` - OAuth2 provider config

### Frontend
- `services/social-auth.service.ts` - Social auth service
- Update login component with social buttons

---

# Feature 2: Product Variants (Size, Color)

## 🎯 Goal
Support product variations like size, color with separate inventory and pricing.

## 📝 Implementation Steps

### Backend Tasks
1. **Create ProductVariant model**
   - SKU, price, stock, attributes (size, color, etc.)
2. **Create VariantAttribute model**
   - Attribute name, values, display order
3. **Update Product model** with variants relationship
4. **Create Variant Service** for CRUD operations
5. **Update Cart/Order** to handle variants
6. **Create Variant Controller**

### Frontend Tasks
1. **Create variant selector component**
2. **Update product detail page**
3. **Update cart to show variant info**
4. **Update vendor product form**

## ⚠️ Potential Problems & Solutions

| Problem | Solution |
|---------|----------|
| SKU generation conflicts | Auto-generate SKU: productId-size-color |
| Inventory tracking per variant | Separate stock field in variant model |
| Price variations | Support base price + variant price offset |
| Out of stock variants | Real-time stock check, disable unavailable |
| Cart with invalid variants | Validate on add and checkout |

## 📁 Files to Create/Modify

### Backend
- `variant/model/ProductVariant.java` - Variant entity
- `variant/model/VariantAttribute.java` - Attribute definition
- `variant/dto/ProductVariantDto.java` - Response DTO
- `variant/dto/CreateVariantRequest.java` - Create request
- `variant/repository/ProductVariantRepository.java`
- `variant/service/ProductVariantService.java`
- `variant/controller/ProductVariantController.java`
- Update `Product.java` with variants
- Update `CartItem.java` with variantId
- Update `OrderItem.java` with variant info

### Frontend
- `services/product-variant.service.ts`
- Components for variant selection

---

# Feature 3: Bulk Upload (CSV Import)

## 🎯 Goal
Allow vendors to upload multiple products via CSV file.

## 📝 Implementation Steps

### Backend Tasks
1. **Add Apache Commons CSV dependency**
2. **Create CSV Parser Service**
   - Parse CSV, validate data, handle errors
3. **Create Bulk Upload Controller**
   - Accept multipart file upload
4. **Create Upload Result DTO**
   - Success count, error count, error details
5. **Implement async processing** for large files
6. **Create sample CSV template**

### Frontend Tasks
1. **Create file upload component**
2. **Show upload progress**
3. **Display results (success/errors)**
4. **Download CSV template**

## ⚠️ Potential Problems & Solutions

| Problem | Solution |
|---------|----------|
| Large file upload timeout | Async processing with status polling |
| Invalid data in CSV | Return detailed row-by-row errors |
| Duplicate products | Check by name+vendor, option to update |
| Missing required fields | Validate and report which rows failed |
| Image URLs in CSV | Download and upload to Cloudinary |
| Memory issues with large files | Stream processing, batch inserts |

## 📁 Files to Create/Modify

### Backend
- `pom.xml` - Add Apache Commons CSV
- `bulkupload/service/CsvParserService.java`
- `bulkupload/service/BulkUploadService.java`
- `bulkupload/controller/BulkUploadController.java`
- `bulkupload/dto/BulkUploadResult.java`
- `bulkupload/dto/ProductCsvRow.java`
- `resources/templates/product-upload-template.csv`

### Frontend
- `services/bulk-upload.service.ts`
- Bulk upload component

---

# Feature 4: Report Generation

## 🎯 Goal
Generate sales reports, tax reports, and analytics for vendors and admins.

## 📝 Implementation Steps

### Backend Tasks
1. **Create Report models**
   - SalesReport, TaxReport, InventoryReport
2. **Create Aggregation queries** for MongoDB
3. **Create Report Service**
   - Generate various report types
4. **Create PDF/Excel export** using Apache POI
5. **Create Report Controller**
6. **Implement report scheduling** (optional)

### Frontend Tasks
1. **Create report dashboard**
2. **Date range selector**
3. **Chart visualizations**
4. **Export buttons (PDF/Excel)**

## ⚠️ Potential Problems & Solutions

| Problem | Solution |
|---------|----------|
| Slow aggregation on large data | Use MongoDB aggregation pipeline, indexes |
| Complex tax calculations | Pre-calculate GST components on orders |
| Report generation timeout | Async generation, download when ready |
| Chart data formatting | Server-side data transformation |
| Multi-vendor data isolation | Filter by vendorId in all queries |

## 📁 Files to Create/Modify

### Backend
- `pom.xml` - Add Apache POI for Excel
- `report/model/SalesReport.java`
- `report/model/TaxReport.java`
- `report/dto/ReportRequest.java`
- `report/dto/SalesReportDto.java`
- `report/dto/TaxReportDto.java`
- `report/service/ReportService.java`
- `report/service/ExcelExportService.java`
- `report/controller/ReportController.java`

### Frontend
- `services/report.service.ts`
- Report dashboard components

---

# Feature 5: Chat Support (Customer-Vendor Messaging)

## 🎯 Goal
Enable real-time messaging between customers and vendors.

## 📝 Implementation Steps

### Backend Tasks
1. **Add WebSocket dependency**
2. **Configure WebSocket/STOMP**
3. **Create ChatMessage model**
4. **Create Conversation model**
5. **Create Chat Service**
6. **Create WebSocket Controller**
7. **Create REST endpoints** for history
8. **Implement message notifications**

### Frontend Tasks
1. **Create chat widget component**
2. **WebSocket connection service**
3. **Chat UI with message list**
4. **Real-time message updates**
5. **Notification badges**

## ⚠️ Potential Problems & Solutions

| Problem | Solution |
|---------|----------|
| WebSocket connection drops | Auto-reconnect with exponential backoff |
| Message ordering | Server-side timestamps, sort on display |
| Offline messages | Store in DB, sync on reconnect |
| Scaling WebSocket | Use Redis pub/sub for multiple instances |
| Message persistence | Save all messages to MongoDB |
| Typing indicators | Separate STOMP topic for typing events |

## 📁 Files to Create/Modify

### Backend
- `pom.xml` - Add spring-boot-starter-websocket
- `chat/config/WebSocketConfig.java`
- `chat/model/ChatMessage.java`
- `chat/model/Conversation.java`
- `chat/dto/ChatMessageDto.java`
- `chat/dto/ConversationDto.java`
- `chat/repository/ChatMessageRepository.java`
- `chat/repository/ConversationRepository.java`
- `chat/service/ChatService.java`
- `chat/controller/ChatController.java`
- `chat/controller/ChatWebSocketController.java`

### Frontend
- `services/chat.service.ts`
- Chat widget components

---

# Feature 6: Return/Refund Module

## 🎯 Goal
Complete return workflow with refund processing.

## 📝 Implementation Steps

### Backend Tasks
1. **Create ReturnRequest model**
   - Status, reason, images, timeline
2. **Create Refund model**
   - Amount, method, status
3. **Create Return Service**
   - Request, approve, reject, process refund
4. **Integrate with Razorpay refund API**
5. **Create Return Controller**
6. **Update Order status** for returns

### Frontend Tasks
1. **Create return request form**
2. **Return status tracking**
3. **Vendor return management**
4. **Admin return dashboard**

## ⚠️ Potential Problems & Solutions

| Problem | Solution |
|---------|----------|
| Partial returns | Support returning individual items |
| Refund calculation | Original price - discounts applied |
| Return window expired | Configurable return policy per product |
| Razorpay refund failure | Retry mechanism, manual refund option |
| Return shipping | Generate return label, track separately |
| Inventory restoration | Add back to stock after return received |

## 📁 Files to Create/Modify

### Backend
- `returns/model/ReturnRequest.java`
- `returns/model/ReturnStatus.java`
- `returns/model/Refund.java`
- `returns/dto/CreateReturnRequest.java`
- `returns/dto/ReturnRequestDto.java`
- `returns/dto/RefundDto.java`
- `returns/repository/ReturnRequestRepository.java`
- `returns/repository/RefundRepository.java`
- `returns/service/ReturnService.java`
- `returns/service/RefundService.java`
- `returns/controller/ReturnController.java`

### Frontend
- `services/return.service.ts`
- Return request components

---

## 🗓️ Implementation Order

1. **Return/Refund Module** - Critical for customer satisfaction
2. **Product Variants** - Essential for product catalog
3. **Social Login** - Improves user onboarding
4. **Bulk Upload** - Vendor efficiency
5. **Report Generation** - Business analytics
6. **Chat Support** - Customer engagement

---

## 📊 Risk Assessment

| Feature | Risk Level | Mitigation |
|---------|------------|------------|
| Social Login | Medium | Test with multiple providers |
| Product Variants | High | Careful cart/order migration |
| Bulk Upload | Medium | Validate thoroughly, async process |
| Report Generation | Medium | Optimize queries, caching |
| Chat Support | High | WebSocket reliability testing |
| Return/Refund | High | Test refund flow with Razorpay sandbox |

---

**Ready to implement! Starting with Feature 1: Social Login**
