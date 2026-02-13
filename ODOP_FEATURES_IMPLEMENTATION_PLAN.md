# ODOP-Specific Features Implementation Plan

## Overview
This document outlines the comprehensive implementation plan for 5 ODOP-specific features that will enhance the platform's focus on India's One District One Product initiative.

---

## Feature 1: District Map Browse (Interactive India Map)

### Description
An interactive SVG/Canvas map of India where users can click on any district to browse products from that specific region.

### Why Important
- Visual discovery of regional products
- Promotes lesser-known district products
- Engaging user experience
- Educational about India's geography and crafts

### Technical Approach

#### Backend Requirements
1. **District Data Model**
   - Store all 766 districts with geo-coordinates
   - Map district codes to state codes
   - Store ODOP product mapping per district

2. **Endpoints Needed**
   - `GET /odop/districts` - List all districts with product counts
   - `GET /odop/districts/{districtCode}` - District details with products
   - `GET /odop/districts/state/{stateCode}` - Districts by state
   - `GET /odop/districts/nearby?lat=&lng=` - Nearby districts
   - `GET /odop/map/stats` - Statistics for map visualization

#### Frontend Requirements
1. **Interactive Map Component**
   - SVG-based India map (TopoJSON)
   - District boundary rendering
   - Color coding based on product availability
   - Hover tooltips with district info
   - Click navigation to district products

2. **Technologies**
   - D3.js for map rendering
   - TopoJSON for district boundaries
   - Angular component wrapper

### Potential Problems & Solutions

| Problem | Solution |
|---------|----------|
| Large SVG file size (~2MB) | Use simplified TopoJSON, lazy load |
| 766 districts slow rendering | Canvas fallback for low-end devices |
| Mobile touch interactions | Implement pinch-zoom, tap-to-select |
| District boundary accuracy | Use official Survey of India data |
| Products not mapped to districts | Migration script to map existing products |

### Implementation Units
1. Create District model and repository
2. Seed district data (all 766 districts)
3. Create district service with aggregations
4. Create district controller endpoints
5. Create Angular map component
6. Implement district selection UI
7. Connect map to product listing

---

## Feature 2: Artisan Stories

### Description
A storytelling platform where artisans can share their craft journey through videos, photos, and text narratives.

### Why Important
- Builds emotional connection with buyers
- Preserves craft heritage documentation
- Differentiates from generic e-commerce
- Increases artisan visibility and trust

### Technical Approach

#### Backend Requirements
1. **Models Needed**
   - `ArtisanStory` - Main story entity
   - `StoryMedia` - Photos/videos with captions
   - `StoryTimeline` - Journey milestones
   - `ArtisanProfile` - Extended vendor profile

2. **Endpoints Needed**
   - `POST /odop/stories` - Create story
   - `GET /odop/stories` - List stories (with filters)
   - `GET /odop/stories/{id}` - Story details
   - `GET /odop/stories/artisan/{vendorId}` - Artisan's stories
   - `GET /odop/stories/featured` - Featured stories
   - `POST /odop/stories/{id}/like` - Like/engage

#### Frontend Requirements
1. **Story Viewer Component**
   - Instagram-style story format
   - Video player with controls
   - Photo gallery with captions
   - Timeline visualization

2. **Story Creator (Vendor)**
   - Multi-media upload
   - Caption/description editor
   - Timeline milestone creator

### Potential Problems & Solutions

| Problem | Solution |
|---------|----------|
| Large video file storage | Cloud storage (Azure Blob/AWS S3) |
| Video transcoding for web | Use FFmpeg or cloud transcoding |
| Slow loading on mobile | Adaptive streaming (HLS), thumbnails |
| Content moderation | Admin approval workflow |
| Multi-language stories | Translation support, regional languages |

### Implementation Units
1. Create ArtisanStory and related models
2. Create story repository with queries
3. Create story service with media handling
4. Create story controller
5. Create Angular story viewer component
6. Create story creator for vendors
7. Add story section to product pages

---

## Feature 3: Craft Categories

### Description
Specialized category taxonomy for Indian crafts: Textiles, Pottery, Jewelry, Food Products, Handicrafts, etc.

### Why Important
- Better product organization
- Easier discovery by craft type
- SEO benefits
- Category-specific attributes

### Technical Approach

#### Backend Requirements
1. **Enhanced Category Model**
   - Hierarchical categories (3 levels)
   - Craft-specific attributes per category
   - Category images and descriptions
   - Related GI tags mapping

2. **Endpoints Needed**
   - `GET /odop/crafts` - All craft categories
   - `GET /odop/crafts/{slug}` - Category with subcategories
   - `GET /odop/crafts/{slug}/products` - Products in category
   - `GET /odop/crafts/{slug}/attributes` - Category-specific filters

#### Frontend Requirements
1. **Craft Category Browser**
   - Visual category cards
   - Subcategory navigation
   - Category-specific filters
   - Related artisans display

### Potential Problems & Solutions

| Problem | Solution |
|---------|----------|
| Migrating existing categories | Mapping script, gradual migration |
| Multi-level navigation UX | Breadcrumbs, sticky navigation |
| Category-specific attributes | Dynamic attribute schema |
| SEO for category pages | Server-side rendering, meta tags |

### Implementation Units
1. Create CraftCategory model
2. Seed craft category data
3. Create category service
4. Create category controller
5. Create Angular category browser
6. Implement category-specific filters
7. Update product forms with craft categories

---

## Feature 4: Festival Collections

### Description
Curated product collections for Indian festivals: Diwali, Durga Puja, Holi, Eid, Christmas, Pongal, etc.

### Why Important
- Seasonal sales boost (festivals = peak shopping)
- Cultural relevance
- Gifting focus
- Marketing opportunities

### Technical Approach

#### Backend Requirements
1. **Models Needed**
   - `FestivalCollection` - Festival details and dates
   - `CollectionProduct` - Products in collection
   - `FestivalBanner` - Promotional banners

2. **Endpoints Needed**
   - `GET /odop/festivals` - All festivals
   - `GET /odop/festivals/upcoming` - Upcoming festivals
   - `GET /odop/festivals/active` - Currently active
   - `GET /odop/festivals/{slug}` - Festival details
   - `GET /odop/festivals/{slug}/products` - Festival products

#### Frontend Requirements
1. **Festival Landing Pages**
   - Festival-themed UI
   - Countdown timers
   - Curated product grids
   - Gift bundles section

### Potential Problems & Solutions

| Problem | Solution |
|---------|----------|
| Variable festival dates (lunar calendar) | Pre-calculate for 5 years, yearly update |
| Regional festival variations | State-wise festival visibility |
| Themed UI changes | CSS variables, theme switching |
| Product curation workload | Auto-suggest based on tags |

### Implementation Units
1. Create FestivalCollection model
2. Seed major Indian festivals data
3. Create festival service
4. Create festival controller
5. Create Angular festival pages
6. Implement countdown and theming
7. Admin panel for collection curation

---

## Feature 5: Government Scheme Links

### Description
Information hub for government schemes benefiting artisans and vendors: PMMY, Mudra Loans, MSME schemes, etc.

### Why Important
- Vendor empowerment
- Financial inclusion
- Government initiative alignment
- Social impact documentation

### Technical Approach

#### Backend Requirements
1. **Models Needed**
   - `GovernmentScheme` - Scheme details
   - `SchemeEligibility` - Eligibility criteria
   - `SchemeApplication` - Application tracking (optional)

2. **Endpoints Needed**
   - `GET /odop/schemes` - All schemes
   - `GET /odop/schemes/{slug}` - Scheme details
   - `GET /odop/schemes/eligible` - Schemes for current vendor
   - `GET /odop/schemes/category/{category}` - By category

#### Frontend Requirements
1. **Scheme Information Pages**
   - Scheme cards with key info
   - Eligibility checker
   - Application links
   - Document requirements

### Potential Problems & Solutions

| Problem | Solution |
|---------|----------|
| Scheme info changes frequently | Admin-editable content, last-updated dates |
| Complex eligibility rules | Rule engine or questionnaire |
| External application links | Open in new tab, track clicks |
| Multi-language content | i18n support, Hindi/English |

### Implementation Units
1. Create GovernmentScheme model
2. Seed major schemes data
3. Create scheme service
4. Create scheme controller
5. Create Angular scheme pages
6. Implement eligibility checker
7. Add scheme section to vendor dashboard

---

## Implementation Priority & Timeline

| Priority | Feature | Complexity | Estimated Time |
|----------|---------|------------|----------------|
| 1 | Craft Categories | Medium | 2-3 hours |
| 2 | Festival Collections | Medium | 2-3 hours |
| 3 | District Map Browse | High | 3-4 hours |
| 4 | Government Schemes | Low | 1-2 hours |
| 5 | Artisan Stories | High | 3-4 hours |

---

## Dependencies Required

### Backend (pom.xml)
```xml
<!-- Already have: Spring Boot, MongoDB, etc. -->
<!-- No additional dependencies needed -->
```

### Frontend (package.json)
```json
{
  "d3": "^7.8.5",
  "topojson-client": "^3.1.0"
}
```

---

## Database Seeding Requirements

1. **Districts**: All 766 districts with coordinates
2. **Craft Categories**: ~50 categories with hierarchy
3. **Festivals**: ~30 major Indian festivals
4. **Government Schemes**: ~20 major schemes

---

## Next Steps

Starting implementation in priority order:
1. ✅ Plan documented
2. 🔄 Implement Craft Categories
3. 🔄 Implement Festival Collections
4. 🔄 Implement District Map
5. 🔄 Implement Government Schemes
6. 🔄 Implement Artisan Stories
