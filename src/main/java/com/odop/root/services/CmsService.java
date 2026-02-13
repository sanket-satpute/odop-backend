package com.odop.root.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.odop.root.models.cms.*;
import com.odop.root.repository.cms.*;

@Service
public class CmsService {

    @Autowired
    private CmsPageRepository pageRepository;

    @Autowired
    private CmsBannerRepository bannerRepository;

    @Autowired
    private CmsFaqRepository faqRepository;

    @Autowired
    private CmsTestimonialRepository testimonialRepository;

    @Autowired
    private SeoSettingsRepository seoSettingsRepository;

    // ==================== PAGES ====================

    public Page<CmsPage> getPages(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (status != null && !status.isEmpty()) {
            return pageRepository.findByStatus(status, pageable);
        }
        return pageRepository.findAll(pageable);
    }

    public List<CmsPage> getAllPages() {
        return pageRepository.findAll(Sort.by("createdAt").descending());
    }

    public Optional<CmsPage> getPageById(String id) {
        return pageRepository.findById(id);
    }

    public Optional<CmsPage> getPageBySlug(String slug) {
        return pageRepository.findBySlug(slug);
    }

    public CmsPage createPage(CmsPage page) {
        page.setCreatedAt(LocalDateTime.now());
        page.setUpdatedAt(LocalDateTime.now());
        if (page.getStatus() == null) {
            page.setStatus("Draft");
        }
        return pageRepository.save(page);
    }

    public CmsPage updatePage(String id, CmsPage pageData) {
        return pageRepository.findById(id).map(page -> {
            page.setTitle(pageData.getTitle());
            page.setSlug(pageData.getSlug());
            page.setType(pageData.getType());
            page.setContent(pageData.getContent());
            page.setMetaTitle(pageData.getMetaTitle());
            page.setMetaDescription(pageData.getMetaDescription());
            page.setMetaKeywords(pageData.getMetaKeywords());
            page.setStatus(pageData.getStatus());
            page.setAuthor(pageData.getAuthor());
            page.setUpdatedAt(LocalDateTime.now());
            return pageRepository.save(page);
        }).orElse(null);
    }

    public void deletePage(String id) {
        pageRepository.deleteById(id);
    }

    public CmsPage publishPage(String id) {
        return pageRepository.findById(id).map(page -> {
            page.setStatus("Published");
            page.setPublishedAt(LocalDateTime.now());
            page.setUpdatedAt(LocalDateTime.now());
            return pageRepository.save(page);
        }).orElse(null);
    }

    // ==================== BANNERS ====================

    public List<CmsBanner> getBanners(boolean activeOnly) {
        if (activeOnly) {
            return bannerRepository.findByActiveTrueOrderByPositionAsc();
        }
        return bannerRepository.findAllByOrderByPositionAsc();
    }

    public Optional<CmsBanner> getBannerById(String id) {
        return bannerRepository.findById(id);
    }

    public CmsBanner createBanner(CmsBanner banner) {
        banner.setCreatedAt(LocalDateTime.now());
        banner.setUpdatedAt(LocalDateTime.now());
        if (banner.getActive() == null) {
            banner.setActive(true);
        }
        if (banner.getPosition() == null) {
            banner.setPosition((int) bannerRepository.count() + 1);
        }
        return bannerRepository.save(banner);
    }

    public CmsBanner updateBanner(String id, CmsBanner bannerData) {
        return bannerRepository.findById(id).map(banner -> {
            banner.setTitle(bannerData.getTitle());
            banner.setImageUrl(bannerData.getImageUrl());
            banner.setMobileImageUrl(bannerData.getMobileImageUrl());
            banner.setLinkUrl(bannerData.getLinkUrl());
            banner.setAltText(bannerData.getAltText());
            banner.setPosition(bannerData.getPosition());
            banner.setActive(bannerData.getActive());
            banner.setStartDate(bannerData.getStartDate());
            banner.setEndDate(bannerData.getEndDate());
            banner.setUpdatedAt(LocalDateTime.now());
            return bannerRepository.save(banner);
        }).orElse(null);
    }

    public void deleteBanner(String id) {
        bannerRepository.deleteById(id);
    }

    public CmsBanner toggleBannerStatus(String id) {
        return bannerRepository.findById(id).map(banner -> {
            banner.setActive(!banner.getActive());
            banner.setUpdatedAt(LocalDateTime.now());
            return bannerRepository.save(banner);
        }).orElse(null);
    }

    // ==================== FAQs ====================

    public List<CmsFaq> getFaqs(String category) {
        if (category != null && !category.isEmpty()) {
            return faqRepository.findByCategoryAndActiveTrue(category);
        }
        return faqRepository.findAllByOrderByPositionAsc();
    }

    public List<CmsFaq> getActiveFaqs() {
        return faqRepository.findByActiveTrueOrderByPositionAsc();
    }

    public Optional<CmsFaq> getFaqById(String id) {
        return faqRepository.findById(id);
    }

    public CmsFaq createFaq(CmsFaq faq) {
        faq.setCreatedAt(LocalDateTime.now());
        faq.setUpdatedAt(LocalDateTime.now());
        if (faq.getActive() == null) {
            faq.setActive(true);
        }
        if (faq.getPosition() == null) {
            faq.setPosition((int) faqRepository.count() + 1);
        }
        return faqRepository.save(faq);
    }

    public CmsFaq updateFaq(String id, CmsFaq faqData) {
        return faqRepository.findById(id).map(faq -> {
            faq.setQuestion(faqData.getQuestion());
            faq.setAnswer(faqData.getAnswer());
            faq.setCategory(faqData.getCategory());
            faq.setPosition(faqData.getPosition());
            faq.setActive(faqData.getActive());
            faq.setUpdatedAt(LocalDateTime.now());
            return faqRepository.save(faq);
        }).orElse(null);
    }

    public void deleteFaq(String id) {
        faqRepository.deleteById(id);
    }

    public CmsFaq toggleFaqStatus(String id) {
        return faqRepository.findById(id).map(faq -> {
            faq.setActive(!faq.getActive());
            faq.setUpdatedAt(LocalDateTime.now());
            return faqRepository.save(faq);
        }).orElse(null);
    }

    // ==================== TESTIMONIALS ====================

    public List<CmsTestimonial> getTestimonials(boolean featuredOnly) {
        if (featuredOnly) {
            return testimonialRepository.findByActiveTrueAndFeaturedTrue();
        }
        return testimonialRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<CmsTestimonial> getActiveTestimonials() {
        return testimonialRepository.findByActiveTrue();
    }

    public Optional<CmsTestimonial> getTestimonialById(String id) {
        return testimonialRepository.findById(id);
    }

    public CmsTestimonial createTestimonial(CmsTestimonial testimonial) {
        testimonial.setCreatedAt(LocalDateTime.now());
        testimonial.setUpdatedAt(LocalDateTime.now());
        if (testimonial.getActive() == null) {
            testimonial.setActive(true);
        }
        if (testimonial.getFeatured() == null) {
            testimonial.setFeatured(false);
        }
        return testimonialRepository.save(testimonial);
    }

    public CmsTestimonial updateTestimonial(String id, CmsTestimonial testimonialData) {
        return testimonialRepository.findById(id).map(testimonial -> {
            testimonial.setName(testimonialData.getName());
            testimonial.setAvatar(testimonialData.getAvatar());
            testimonial.setDesignation(testimonialData.getDesignation());
            testimonial.setCompany(testimonialData.getCompany());
            testimonial.setRating(testimonialData.getRating());
            testimonial.setText(testimonialData.getText());
            testimonial.setImageUrl(testimonialData.getImageUrl());
            testimonial.setActive(testimonialData.getActive());
            testimonial.setFeatured(testimonialData.getFeatured());
            testimonial.setUpdatedAt(LocalDateTime.now());
            return testimonialRepository.save(testimonial);
        }).orElse(null);
    }

    public void deleteTestimonial(String id) {
        testimonialRepository.deleteById(id);
    }

    public CmsTestimonial toggleTestimonialStatus(String id) {
        return testimonialRepository.findById(id).map(testimonial -> {
            testimonial.setActive(!testimonial.getActive());
            testimonial.setUpdatedAt(LocalDateTime.now());
            return testimonialRepository.save(testimonial);
        }).orElse(null);
    }

    // ==================== SEO SETTINGS ====================

    public SeoSettings getSeoSettings() {
        List<SeoSettings> all = seoSettingsRepository.findAll();
        if (all.isEmpty()) {
            return createDefaultSeoSettings();
        }
        return all.get(0);
    }

    public SeoSettings updateSeoSettings(SeoSettings settings) {
        List<SeoSettings> all = seoSettingsRepository.findAll();
        if (all.isEmpty()) {
            return seoSettingsRepository.save(settings);
        }
        SeoSettings existing = all.get(0);
        existing.setHomepageTitle(settings.getHomepageTitle());
        existing.setHomepageDescription(settings.getHomepageDescription());
        existing.setHomepageKeywords(settings.getHomepageKeywords());
        existing.setBlogTitle(settings.getBlogTitle());
        existing.setBlogDescription(settings.getBlogDescription());
        existing.setBlogKeywords(settings.getBlogKeywords());
        existing.setCategoryTitleTemplate(settings.getCategoryTitleTemplate());
        existing.setProductTitleTemplate(settings.getProductTitleTemplate());
        existing.setDefaultAuthor(settings.getDefaultAuthor());
        existing.setRobotsTxt(settings.getRobotsTxt());
        existing.setGoogleAnalyticsId(settings.getGoogleAnalyticsId());
        existing.setGoogleSearchConsole(settings.getGoogleSearchConsole());
        existing.setFacebookPixelId(settings.getFacebookPixelId());
        existing.setTwitterHandle(settings.getTwitterHandle());
        existing.setOgDefaultImage(settings.getOgDefaultImage());
        return seoSettingsRepository.save(existing);
    }

    private SeoSettings createDefaultSeoSettings() {
        SeoSettings settings = new SeoSettings();
        settings.setHomepageTitle("ODOP - One District One Product | Authentic Indian Crafts");
        settings.setHomepageDescription("Discover authentic handcrafted products from across India through our One District One Product initiative.");
        settings.setHomepageKeywords("ODOP, Indian crafts, handmade, artisan products, traditional crafts");
        settings.setBlogTitle("ODOP Blog - Stories of Indian Artisans");
        settings.setBlogDescription("Explore stories, insights, and news about Indian artisans and their crafts.");
        settings.setBlogKeywords("artisan stories, Indian crafts blog, ODOP news");
        settings.setCategoryTitleTemplate("{category} - ODOP Products");
        settings.setProductTitleTemplate("{product} | {category} - ODOP");
        settings.setDefaultAuthor("ODOP Team");
        settings.setRobotsTxt("User-agent: *\nAllow: /\nSitemap: https://odop.in/sitemap.xml");
        return seoSettingsRepository.save(settings);
    }

    // ==================== STATS ====================

    public CmsStats getStats() {
        long totalPages = pageRepository.count();
        long publishedPages = pageRepository.countByStatus("Published");
        long draftPages = pageRepository.countByStatus("Draft");
        long totalBanners = bannerRepository.count();
        long activeBanners = bannerRepository.countByActiveTrue();
        long totalFaqs = faqRepository.count();
        long activeFaqs = faqRepository.countByActiveTrue();
        long totalTestimonials = testimonialRepository.count();
        long activeTestimonials = testimonialRepository.countByActiveTrue();
        long featuredTestimonials = testimonialRepository.countByFeaturedTrue();
        
        return new CmsStats(
            totalPages, publishedPages, draftPages,
            totalBanners, activeBanners,
            totalFaqs, activeFaqs,
            totalTestimonials, activeTestimonials, featuredTestimonials
        );
    }

    // Stats record
    public record CmsStats(
        long totalPages, long publishedPages, long draftPages,
        long totalBanners, long activeBanners,
        long totalFaqs, long activeFaqs,
        long totalTestimonials, long activeTestimonials, long featuredTestimonials
    ) {}
}
