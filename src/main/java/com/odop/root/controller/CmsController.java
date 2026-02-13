package com.odop.root.controller;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.odop.root.models.cms.*;
import com.odop.root.services.CmsService;

@RestController
@RequestMapping("odop/cms")
@CrossOrigin
public class CmsController {

    @Autowired
    private CmsService cmsService;

    // ==================== PAGES ====================

    @GetMapping("/pages")
    public ResponseEntity<Map<String, Object>> getPages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        Page<CmsPage> pageResult = cmsService.getPages(page, size, status);
        return ResponseEntity.ok(buildListResponse(pageResult.getContent(), pageResult.getTotalElements(), page, size));
    }

    @GetMapping("/pages/{id}")
    public ResponseEntity<CmsPage> getPageById(@PathVariable String id) {
        return cmsService.getPageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pages/slug/{slug}")
    public ResponseEntity<CmsPage> getPageBySlug(@PathVariable String slug) {
        return cmsService.getPageBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/pages")
    public ResponseEntity<CmsPage> createPage(@RequestBody CmsPage page) {
        CmsPage created = cmsService.createPage(page);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/pages/{id}")
    public ResponseEntity<CmsPage> updatePage(@PathVariable String id, @RequestBody CmsPage page) {
        CmsPage updated = cmsService.updatePage(id, page);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/pages/{id}")
    public ResponseEntity<Void> deletePage(@PathVariable String id) {
        cmsService.deletePage(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/pages/{id}/publish")
    public ResponseEntity<CmsPage> publishPage(@PathVariable String id) {
        CmsPage published = cmsService.publishPage(id);
        if (published != null) {
            return ResponseEntity.ok(published);
        }
        return ResponseEntity.notFound().build();
    }

    // ==================== BANNERS ====================

    @GetMapping("/banners")
    public ResponseEntity<Map<String, Object>> getBanners(
            @RequestParam(required = false) String active) {
        boolean activeOnly = "true".equalsIgnoreCase(active);
        List<CmsBanner> banners = cmsService.getBanners(activeOnly);
        return ResponseEntity.ok(buildListResponse(banners, banners.size(), 0, banners.size()));
    }

    @GetMapping("/banners/{id}")
    public ResponseEntity<CmsBanner> getBannerById(@PathVariable String id) {
        return cmsService.getBannerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/banners")
    public ResponseEntity<CmsBanner> createBanner(@RequestBody CmsBanner banner) {
        CmsBanner created = cmsService.createBanner(banner);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/banners/{id}")
    public ResponseEntity<CmsBanner> updateBanner(@PathVariable String id, @RequestBody CmsBanner banner) {
        CmsBanner updated = cmsService.updateBanner(id, banner);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/banners/{id}")
    public ResponseEntity<Void> deleteBanner(@PathVariable String id) {
        cmsService.deleteBanner(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/banners/{id}/toggle")
    public ResponseEntity<CmsBanner> toggleBannerStatus(@PathVariable String id) {
        CmsBanner toggled = cmsService.toggleBannerStatus(id);
        if (toggled != null) {
            return ResponseEntity.ok(toggled);
        }
        return ResponseEntity.notFound().build();
    }

    // ==================== FAQs ====================

    @GetMapping("/faqs")
    public ResponseEntity<Map<String, Object>> getFaqs(
            @RequestParam(required = false) String category) {
        List<CmsFaq> faqs = cmsService.getFaqs(category);
        return ResponseEntity.ok(buildListResponse(faqs, faqs.size(), 0, faqs.size()));
    }

    @GetMapping("/faqs/active")
    public ResponseEntity<List<CmsFaq>> getActiveFaqs() {
        return ResponseEntity.ok(cmsService.getActiveFaqs());
    }

    @GetMapping("/faqs/{id}")
    public ResponseEntity<CmsFaq> getFaqById(@PathVariable String id) {
        return cmsService.getFaqById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/faqs")
    public ResponseEntity<CmsFaq> createFaq(@RequestBody CmsFaq faq) {
        CmsFaq created = cmsService.createFaq(faq);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/faqs/{id}")
    public ResponseEntity<CmsFaq> updateFaq(@PathVariable String id, @RequestBody CmsFaq faq) {
        CmsFaq updated = cmsService.updateFaq(id, faq);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/faqs/{id}")
    public ResponseEntity<Void> deleteFaq(@PathVariable String id) {
        cmsService.deleteFaq(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/faqs/{id}/toggle")
    public ResponseEntity<CmsFaq> toggleFaqStatus(@PathVariable String id) {
        CmsFaq toggled = cmsService.toggleFaqStatus(id);
        if (toggled != null) {
            return ResponseEntity.ok(toggled);
        }
        return ResponseEntity.notFound().build();
    }

    // ==================== TESTIMONIALS ====================

    @GetMapping("/testimonials")
    public ResponseEntity<Map<String, Object>> getTestimonials(
            @RequestParam(required = false) String featured) {
        boolean featuredOnly = "true".equalsIgnoreCase(featured);
        List<CmsTestimonial> testimonials = cmsService.getTestimonials(featuredOnly);
        return ResponseEntity.ok(buildListResponse(testimonials, testimonials.size(), 0, testimonials.size()));
    }

    @GetMapping("/testimonials/active")
    public ResponseEntity<List<CmsTestimonial>> getActiveTestimonials() {
        return ResponseEntity.ok(cmsService.getActiveTestimonials());
    }

    @GetMapping("/testimonials/{id}")
    public ResponseEntity<CmsTestimonial> getTestimonialById(@PathVariable String id) {
        return cmsService.getTestimonialById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/testimonials")
    public ResponseEntity<CmsTestimonial> createTestimonial(@RequestBody CmsTestimonial testimonial) {
        CmsTestimonial created = cmsService.createTestimonial(testimonial);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/testimonials/{id}")
    public ResponseEntity<CmsTestimonial> updateTestimonial(@PathVariable String id, @RequestBody CmsTestimonial testimonial) {
        CmsTestimonial updated = cmsService.updateTestimonial(id, testimonial);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/testimonials/{id}")
    public ResponseEntity<Void> deleteTestimonial(@PathVariable String id) {
        cmsService.deleteTestimonial(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/testimonials/{id}/toggle")
    public ResponseEntity<CmsTestimonial> toggleTestimonialStatus(@PathVariable String id) {
        CmsTestimonial toggled = cmsService.toggleTestimonialStatus(id);
        if (toggled != null) {
            return ResponseEntity.ok(toggled);
        }
        return ResponseEntity.notFound().build();
    }

    // ==================== SEO SETTINGS ====================

    @GetMapping("/seo")
    public ResponseEntity<SeoSettings> getSeoSettings() {
        return ResponseEntity.ok(cmsService.getSeoSettings());
    }

    @PutMapping("/seo")
    public ResponseEntity<SeoSettings> updateSeoSettings(@RequestBody SeoSettings settings) {
        SeoSettings updated = cmsService.updateSeoSettings(settings);
        return ResponseEntity.ok(updated);
    }

    // ==================== STATS ====================

    @GetMapping("/stats")
    public ResponseEntity<CmsService.CmsStats> getStats() {
        return ResponseEntity.ok(cmsService.getStats());
    }

    // ==================== HELPER METHODS ====================

    private <T> Map<String, Object> buildListResponse(List<T> data, long total, int page, int pageSize) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("total", total);
        response.put("page", page);
        response.put("pageSize", pageSize);
        return response;
    }
}
