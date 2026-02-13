package com.odop.root.config;

import com.odop.root.filter.JwtRequestFilter;
import com.odop.root.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    // ✅ SOLUTION: Completely bypass Spring Security for this endpoint
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().requestMatchers("/odop/customer/check_customer_exists/**");
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = 
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // ✅ TEMPORARY: Test rule - permit ALL access to this specific endpoint
                        .requestMatchers(HttpMethod.GET, "/odop/customer/check_customer_exists/**").permitAll()
                        // Permits access to specific endpoints without authentication
                        .requestMatchers("/authenticate", "/odop/admin/create_account", "/odop/admin/check_admin_exists", "/odop/customer/create_account", "/odop/vendor/create_account", "/odop/customer/check_customer_exists/**").permitAll()
                        // ✅ Social Login endpoints - allow public access
                        .requestMatchers("/odop/auth/social/login", "/odop/auth/social/health").permitAll()
                        .requestMatchers("/odop/auth/social/**").authenticated()
                        // ✅ Admin management endpoints
                        .requestMatchers("/odop/admin/create_account", "/odop/admin/check_admin_exists").permitAll()
                        .requestMatchers("/odop/admin/**").hasRole("ADMIN")
                        // ✅ Review moderation endpoints - admin only
                        .requestMatchers("/odop/review/admin/**").hasRole("ADMIN")
                        // ✅ Product admin endpoints - admin only
                        .requestMatchers("/odop/product/admin/**").hasRole("ADMIN")
                        // ✅ Payment endpoints - allow create-order and verify for checkout flow
                        .requestMatchers("/odop/payment/create-order", "/odop/payment/verify", "/odop/payment/health").permitAll()
                        // ✅ Email endpoints - allow health, test, and welcome endpoints for registration flow
                        .requestMatchers("/odop/email/health", "/odop/email/test", "/odop/email/test/**").permitAll()
                        .requestMatchers("/odop/email/welcome", "/odop/email/welcome/async").permitAll()
                        // Other email sending requires authentication
                        .requestMatchers("/odop/email/**").authenticated()
                        // ✅ OTP endpoints - allow without authentication for phone verification
                        .requestMatchers("/odop/otp/send", "/odop/otp/verify", "/odop/otp/resend", "/odop/otp/health", "/odop/otp/check-verified").permitAll()
                        // ✅ Image endpoints - allow health and GET operations, uploads require auth
                        .requestMatchers("/odop/images/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/images/**").permitAll()
                        .requestMatchers("/odop/images/**").authenticated()
                        // ✅ Search endpoints - allow public access for search, autocomplete, filters
                        .requestMatchers("/odop/search", "/odop/search/health", "/odop/search/vendors").permitAll()
                        .requestMatchers("/odop/search/autocomplete", "/odop/search/trending", "/odop/search/filters").permitAll()
                        // Search sync endpoints require admin
                        .requestMatchers("/odop/search/sync/**").hasRole("ADMIN")
                        // ✅ Analytics endpoints - dashboard for admin, vendor-specific for vendors
                        .requestMatchers("/odop/analytics/health").permitAll()
                        .requestMatchers("/odop/analytics/dashboard/**").hasRole("ADMIN")
                        .requestMatchers("/odop/analytics/sales").hasRole("ADMIN")
                        .requestMatchers("/odop/analytics/geographic").hasRole("ADMIN")
                        .requestMatchers("/odop/analytics/vendors/**").hasRole("ADMIN")
                        .requestMatchers("/odop/analytics/odop/**").hasRole("ADMIN")
                        .requestMatchers("/odop/analytics/vendor/{vendorId}/**").authenticated()
                        // ✅ Wishlist endpoints - health public, rest require customer auth
                        .requestMatchers("/odop/wishlist/health").permitAll()
                        .requestMatchers("/odop/wishlist/**").hasRole("CUSTOMER")
                        // ✅ Coupon endpoints - admin manages, customers can validate/view
                        .requestMatchers("/odop/coupon/health").permitAll()
                        .requestMatchers("/odop/coupon/available").permitAll()
                        .requestMatchers("/odop/coupon/validate").hasRole("CUSTOMER")
                        .requestMatchers("/odop/coupon/**").hasRole("ADMIN")
                        // ✅ Product Variant endpoints - public read, vendor/admin write
                        .requestMatchers(HttpMethod.GET, "/odop/variants/**").permitAll()
                        .requestMatchers("/odop/variants/attributes/init").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/odop/variants/attributes").hasRole("ADMIN")
                        .requestMatchers("/odop/variants/**").authenticated()
                        // ✅ Bulk Upload endpoints - vendor only
                        .requestMatchers("/odop/bulk-upload/health").permitAll()
                        .requestMatchers("/odop/bulk-upload/**").hasRole("VENDOR")
                        // ✅ Report endpoints - admin and vendor
                        .requestMatchers("/odop/reports/health", "/odop/reports/formats").permitAll()
                        .requestMatchers("/odop/reports/**").authenticated()
                        // ✅ Chat endpoints - authenticated users
                        .requestMatchers("/odop/chat/health").permitAll()
                        .requestMatchers("/ws/chat/**").permitAll()  // WebSocket endpoint
                        .requestMatchers("/odop/chat/support/**").hasAnyRole("ADMIN", "SUPPORT")
                        .requestMatchers("/odop/chat/**").authenticated()
                        // ✅ Returns/Refund endpoints
                        .requestMatchers("/odop/returns/health", "/odop/returns/policy").permitAll()
                        .requestMatchers("/odop/returns/admin/**").hasRole("ADMIN")
                        .requestMatchers("/odop/returns/vendor/**").hasRole("VENDOR")
                        .requestMatchers("/odop/returns/my-returns").hasRole("CUSTOMER")
                        .requestMatchers("/odop/returns/**").authenticated()
                        // ✅ Shipping endpoints - public tracking, role-based access for vendor/customer
                        .requestMatchers("/odop/shipping/track/**").permitAll()
                        .requestMatchers("/odop/shipping/vendor/**").hasRole("VENDOR")
                        .requestMatchers("/odop/shipping/customer/**").hasRole("CUSTOMER")
                        .requestMatchers("/odop/shipping/**").authenticated()
                        // ✅ Vendor Earnings endpoints - vendor only
                        .requestMatchers("/odop/earnings/health").permitAll()
                        .requestMatchers("/odop/earnings/vendor/**").hasRole("VENDOR")
                        .requestMatchers("/odop/earnings/**").authenticated()
                        // ✅ Certifications endpoints - public health, vendor manages their own
                        .requestMatchers("/odop/certifications/health").permitAll()
                        .requestMatchers("/odop/certifications/types").permitAll()
                        .requestMatchers("/odop/certifications/vendor/**").hasRole("VENDOR")
                        .requestMatchers("/odop/certifications/**").authenticated()
                        // ✅ Craft Category endpoints - public read, admin write
                        .requestMatchers(HttpMethod.GET, "/odop/craft-categories/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/odop/craft-categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/odop/craft-categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/odop/craft-categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/odop/craft-categories/**").hasRole("ADMIN")
                        // ✅ Festival Collections endpoints - public read, admin write
                        .requestMatchers(HttpMethod.GET, "/odop/festivals/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/odop/festivals/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/odop/festivals/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/odop/festivals/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/odop/festivals/**").hasRole("ADMIN")
                        // ✅ District Map endpoints - public read, admin write
                        .requestMatchers(HttpMethod.GET, "/odop/district-map/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/odop/district-map/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/odop/district-map/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/odop/district-map/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/odop/district-map/**").hasRole("ADMIN")
                        // ✅ Government Schemes endpoints - public read/search/finder, admin write
                        .requestMatchers(HttpMethod.GET, "/odop/schemes/**").permitAll()
                        .requestMatchers("/odop/schemes/finder").permitAll()
                        .requestMatchers("/odop/schemes/*/track-apply").permitAll()
                        .requestMatchers(HttpMethod.POST, "/odop/schemes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/odop/schemes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/odop/schemes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/odop/schemes/**").hasRole("ADMIN")
                        // ✅ Artisan Stories endpoints - public read/search, admin write
                        .requestMatchers(HttpMethod.GET, "/odop/artisans/**").permitAll()
                        .requestMatchers("/odop/artisans/*/share").permitAll()
                        .requestMatchers("/odop/artisans/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/odop/artisans/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/odop/artisans/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/odop/artisans/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/odop/artisans/**").hasRole("ADMIN")
                        // Payment status and history require authentication
                        .requestMatchers("/odop/payment/**").authenticated()
                        // ✅ Order endpoints - vendor can view their orders
                        .requestMatchers("/odop/order/vendor/**").hasRole("VENDOR")
                        .requestMatchers("/odop/order/customer/**").hasRole("CUSTOMER")
                        .requestMatchers("/odop/order/**").authenticated()
                        // ✅ PUBLIC Product endpoints - allow browsing without auth
                        .requestMatchers(HttpMethod.GET, "/odop/product/featured").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/product/get_all_products").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/product/get_product_id/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/product/get_product_by_vendor_id/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/product/get_product_by_product_name/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/product/get_product_by_category_id/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/product/search/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/product/category/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/product/vendor/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/product/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/product/details/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/product/filter").permitAll()
                        .requestMatchers(HttpMethod.POST, "/odop/product/filter").permitAll()
                        // ✅ PUBLIC Vendor endpoints - allow browsing vendors without auth
                        .requestMatchers(HttpMethod.GET, "/odop/vendor/get_all_vendors").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/vendor/get_vendor_id/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/vendor/featured").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/vendor/search/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/vendor/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/vendor/details/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/vendor/by-district/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/vendor/by-category/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/vendor/search_by_state**").permitAll()
                        // ✅ Admin can update vendor status (before general vendor rule)
                        .requestMatchers(HttpMethod.PATCH, "/odop/vendor/update_status/**").hasRole("ADMIN")
                        // ✅ PUBLIC Category endpoints - allow browsing categories
                        .requestMatchers(HttpMethod.GET, "/odop/category/**").permitAll()
                        // ✅ PUBLIC Review endpoints - allow reading reviews
                        .requestMatchers(HttpMethod.GET, "/odop/review/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/reviews/**").permitAll()
                        // ✅ CMS endpoints - public read for published content, admin write
                        .requestMatchers(HttpMethod.GET, "/odop/cms/pages/slug/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/cms/banners").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/cms/faqs").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/cms/faqs/active").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/cms/testimonials").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/cms/testimonials/active").permitAll()
                        .requestMatchers(HttpMethod.GET, "/odop/cms/seo").permitAll()
                        .requestMatchers("/odop/cms/**").hasRole("ADMIN")
                        // ✅ Platform Settings endpoints - public endpoints, admin for management
                        .requestMatchers("/odop/settings/public/**").permitAll()
                        .requestMatchers("/odop/settings/**").hasRole("ADMIN")
                        // Role-based access for admin endpoints
                        .requestMatchers("/odop/admin/**").hasRole("ADMIN")
                        .requestMatchers("/odop/customer/**").hasRole("CUSTOMER")
                        .requestMatchers("/odop/vendor/**").hasRole("VENDOR")
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
