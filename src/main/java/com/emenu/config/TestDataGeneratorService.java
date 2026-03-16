package com.emenu.config;

import com.emenu.enums.product.ProductStatus;
import com.emenu.enums.product.PromotionType;
import com.emenu.enums.user.BusinessStatus;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.main.models.Brand;
import com.emenu.features.main.models.Category;
import com.emenu.features.main.models.Product;
import com.emenu.features.main.models.ProductImage;
import com.emenu.features.main.models.ProductSize;
import com.emenu.features.main.repository.BrandRepository;
import com.emenu.features.main.repository.CategoryRepository;
import com.emenu.features.main.repository.ProductImageRepository;
import com.emenu.features.main.repository.ProductRepository;
import com.emenu.features.main.repository.ProductSizeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test Data Generator Service
 * Generates realistic sample businesses, categories, brands, and products with Freepik images
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class TestDataGeneratorService {

    private final BusinessRepository businessRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;
    private final ProductImageRepository productImageRepository;

    private static final AtomicBoolean testDataInitialized = new AtomicBoolean(false);

    @Value("${app.init.generate-test-data:true}")
    private boolean generateTestData;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void generateInitialTestData() {
        if (!generateTestData) {
            log.info("Test data generation disabled");
            return;
        }

        if (testDataInitialized.get()) {
            log.info("Test data already generated. Skipping...");
            return;
        }

        synchronized (testDataInitialized) {
            if (testDataInitialized.get()) {
                log.info("Test data already generated (double-check). Skipping...");
                return;
            }

            try {
                log.info("🚀 Starting test data generation with Freepik images...");

                generateTestDataWithFreepikImages();

                testDataInitialized.set(true);
                log.info("🎉 Test data generation completed successfully!");

            } catch (Exception e) {
                log.error("❌ Error during test data generation: {}", e.getMessage(), e);
                throw new RuntimeException("Test data generation failed", e);
            }
        }
    }

    private void generateTestDataWithFreepikImages() {
        // Generate sample business
        Business business = generateSampleBusiness();
        log.info("✅ Created sample business: {}", business.getName());

        // Generate categories
        List<Category> categories = generateCategories();
        log.info("✅ Created {} categories", categories.size());

        // Generate brands
        List<Brand> brands = generateBrands();
        log.info("✅ Created {} brands", brands.size());

        // Generate products with Freepik images
        generateProducts(business, categories, brands);
        log.info("✅ Created products with sample data");
    }

    private Business generateSampleBusiness() {
        if (businessRepository.findByNameAndIsDeletedFalse("Downtown Cafe").isPresent()) {
            log.info("ℹ️ Sample business already exists");
            return businessRepository.findByNameAndIsDeletedFalse("Downtown Cafe").get();
        }

        Business business = new Business();
        business.setName("Downtown Cafe");
        business.setEmail("downtown@cafe.com");
        business.setPhone("+855 23 888 999");
        business.setAddress("123 Norodom Boulevard, Phnom Penh, Cambodia");
        business.setDescription("Premium cafe serving authentic Cambodian and international cuisines");
        business.setStatus(BusinessStatus.ACTIVE);
        business.setIsSubscriptionActive(true);

        return businessRepository.save(business);
    }

    private List<Category> generateCategories() {
        List<Category> categories = new ArrayList<>();

        String[] categoryNames = {"Beverages", "Desserts", "Main Courses", "Appetizers", "Salads"};

        for (String name : categoryNames) {
            if (categoryRepository.findByNameAndIsDeletedFalse(name).isEmpty()) {
                Category category = new Category();
                category.setName(name);
                category.setDescription(name + " category with delicious items");
                category.setImageUrl(getFreepikImageForCategory(name));
                categories.add(categoryRepository.save(category));
            } else {
                categories.add(categoryRepository.findByNameAndIsDeletedFalse(name).get());
            }
        }

        return categories;
    }

    private List<Brand> generateBrands() {
        List<Brand> brands = new ArrayList<>();

        String[] brandNames = {"Premium Roast", "Local Harvest", "Artisan Blend", "Heritage Taste"};

        for (String name : brandNames) {
            if (brandRepository.findByNameAndIsDeletedFalse(name).isEmpty()) {
                Brand brand = new Brand();
                brand.setName(name);
                brand.setDescription(name + " - Premium quality brand");
                brand.setImageUrl(getFreepikImageForBrand(name));
                brands.add(brandRepository.save(brand));
            } else {
                brands.add(brandRepository.findByNameAndIsDeletedFalse(name).get());
            }
        }

        return brands;
    }

    private void generateProducts(Business business, List<Category> categories, List<Brand> brands) {
        generateCoffeeProducts(business, categories, brands);
        generateDessertProducts(business, categories, brands);
        generateMainCourseProducts(business, categories, brands);
    }

    private void generateCoffeeProducts(Business business, List<Category> categories, List<Brand> brands) {
        Category beverageCategory = categories.get(0); // Beverages
        Brand premiumRoast = brands.get(0); // Premium Roast

        // Iced Latte
        if (productRepository.findByNameAndBusinessIdAndIsDeletedFalse("Iced Latte", business.getId()).isEmpty()) {
            Product icedLatte = new Product();
            icedLatte.setBusinessId(business.getId());
            icedLatte.setName("Iced Latte");
            icedLatte.setDescription("Smooth and creamy iced latte with premium espresso and fresh milk");
            icedLatte.setCategoryId(beverageCategory.getId());
            icedLatte.setBrandId(premiumRoast.getId());
            icedLatte.setStatus(ProductStatus.ACTIVE);
            icedLatte.setMainImageUrl("https://images.freepik.com/free-photo/coffee-latte-cup-with-ice_1134-1276.jpg");

            icedLatte = productRepository.save(icedLatte);

            // Add product sizes
            addProductSizes(icedLatte,
                new SizeData("Small", new BigDecimal("3.50")),
                new SizeData("Medium", new BigDecimal("4.50")),
                new SizeData("Large", new BigDecimal("5.50"))
            );

            // Add promotion
            addPromotion(icedLatte, PromotionType.PERCENTAGE, new BigDecimal("10"));

            // Add images
            addProductImages(icedLatte,
                "https://images.freepik.com/free-photo/iced-coffee-with-straw-glass_1134-1987.jpg",
                "https://images.freepik.com/free-photo/refreshing-iced-coffee-summer-drink_1134-2456.jpg",
                "https://images.freepik.com/free-photo/cold-iced-latte-coffee-drink_1134-1567.jpg"
            );
        }

        // Espresso
        if (productRepository.findByNameAndBusinessIdAndIsDeletedFalse("Espresso", business.getId()).isEmpty()) {
            Product espresso = new Product();
            espresso.setBusinessId(business.getId());
            espresso.setName("Espresso");
            espresso.setDescription("Strong and bold single shot of premium espresso");
            espresso.setCategoryId(beverageCategory.getId());
            espresso.setBrandId(premiumRoast.getId());
            espresso.setStatus(ProductStatus.ACTIVE);
            espresso.setMainImageUrl("https://images.freepik.com/free-photo/small-cup-espresso-coffee_1134-456.jpg");

            espresso = productRepository.save(espresso);

            addProductSizes(espresso,
                new SizeData("Single Shot", new BigDecimal("2.00")),
                new SizeData("Double Shot", new BigDecimal("3.00"))
            );

            addProductImages(espresso,
                "https://images.freepik.com/free-photo/hot-espresso-coffee-cup_1134-789.jpg",
                "https://images.freepik.com/free-photo/espresso-coffee-beans-cup_1134-1234.jpg",
                "https://images.freepik.com/free-photo/black-espresso-coffee_1134-2012.jpg"
            );
        }

        // Cappuccino
        if (productRepository.findByNameAndBusinessIdAndIsDeletedFalse("Cappuccino", business.getId()).isEmpty()) {
            Product cappuccino = new Product();
            cappuccino.setBusinessId(business.getId());
            cappuccino.setName("Cappuccino");
            cappuccino.setDescription("Classic cappuccino with espresso, steamed milk and foam");
            cappuccino.setCategoryId(beverageCategory.getId());
            cappuccino.setBrandId(premiumRoast.getId());
            cappuccino.setStatus(ProductStatus.ACTIVE);
            cappuccino.setMainImageUrl("https://images.freepik.com/free-photo/cappuccino-with-latte-foam_1134-567.jpg");

            cappuccino = productRepository.save(cappuccino);

            addProductSizes(cappuccino,
                new SizeData("Small", new BigDecimal("4.00")),
                new SizeData("Medium", new BigDecimal("4.50")),
                new SizeData("Large", new BigDecimal("5.00"))
            );

            addPromotion(cappuccino, PromotionType.FIXED_AMOUNT, new BigDecimal("0.75"));

            addProductImages(cappuccino,
                "https://images.freepik.com/free-photo/cappuccino-coffee-with-foam-art_1134-890.jpg",
                "https://images.freepik.com/free-photo/creamy-cappuccino-latte_1134-1456.jpg",
                "https://images.freepik.com/free-photo/coffee-cappuccino-with-milk-foam_1134-2789.jpg"
            );
        }
    }

    private void generateDessertProducts(Business business, List<Category> categories, List<Brand> brands) {
        Category dessertCategory = categories.get(1); // Desserts
        Brand artisanBlend = brands.get(2); // Artisan Blend

        // Chocolate Cake
        if (productRepository.findByNameAndBusinessIdAndIsDeletedFalse("Chocolate Cake", business.getId()).isEmpty()) {
            Product chocolateCake = new Product();
            chocolateCake.setBusinessId(business.getId());
            chocolateCake.setName("Chocolate Cake");
            chocolateCake.setDescription("Rich and moist chocolate cake with dark chocolate frosting");
            chocolateCake.setCategoryId(dessertCategory.getId());
            chocolateCake.setBrandId(artisanBlend.getId());
            chocolateCake.setStatus(ProductStatus.ACTIVE);
            chocolateCake.setMainImageUrl("https://images.freepik.com/free-photo/chocolate-cake-slice-with-fork_1134-3456.jpg");

            chocolateCake = productRepository.save(chocolateCake);

            addProductSizes(chocolateCake,
                new SizeData("Slice", new BigDecimal("5.00")),
                new SizeData("Whole Cake", new BigDecimal("25.00"))
            );

            addProductImages(chocolateCake,
                "https://images.freepik.com/free-photo/homemade-chocolate-cake_1134-4567.jpg",
                "https://images.freepik.com/free-photo/dark-chocolate-cake-dessert_1134-5678.jpg",
                "https://images.freepik.com/free-photo/rich-chocolate-layer-cake_1134-6789.jpg"
            );
        }

        // Cheesecake
        if (productRepository.findByNameAndBusinessIdAndIsDeletedFalse("Cheesecake", business.getId()).isEmpty()) {
            Product cheesecake = new Product();
            cheesecake.setBusinessId(business.getId());
            cheesecake.setName("Cheesecake");
            cheesecake.setDescription("Classic New York style cheesecake with graham cracker crust");
            cheesecake.setCategoryId(dessertCategory.getId());
            cheesecake.setBrandId(artisanBlend.getId());
            cheesecake.setStatus(ProductStatus.ACTIVE);
            cheesecake.setMainImageUrl("https://images.freepik.com/free-photo/delicious-cheesecake-slice_1134-2345.jpg");

            cheesecake = productRepository.save(cheesecake);

            addProductSizes(cheesecake,
                new SizeData("Slice", new BigDecimal("6.00")),
                new SizeData("Whole Cake", new BigDecimal("28.00"))
            );

            addPromotion(cheesecake, PromotionType.PERCENTAGE, new BigDecimal("15"));

            addProductImages(cheesecake,
                "https://images.freepik.com/free-photo/creamy-cheesecake-dessert_1134-3456.jpg",
                "https://images.freepik.com/free-photo/strawberry-cheesecake_1134-4567.jpg",
                "https://images.freepik.com/free-photo/ny-style-cheesecake-slice_1134-5678.jpg"
            );
        }

        // Tiramisu
        if (productRepository.findByNameAndBusinessIdAndIsDeletedFalse("Tiramisu", business.getId()).isEmpty()) {
            Product tiramisu = new Product();
            tiramisu.setBusinessId(business.getId());
            tiramisu.setName("Tiramisu");
            tiramisu.setDescription("Authentic Italian tiramisu with mascarpone and coffee");
            tiramisu.setCategoryId(dessertCategory.getId());
            tiramisu.setBrandId(artisanBlend.getId());
            tiramisu.setStatus(ProductStatus.ACTIVE);
            tiramisu.setMainImageUrl("https://images.freepik.com/free-photo/italian-tiramisu-dessert_1134-6789.jpg");

            tiramisu = productRepository.save(tiramisu);

            addProductSizes(tiramisu,
                new SizeData("Slice", new BigDecimal("5.50")),
                new SizeData("Box", new BigDecimal("22.00"))
            );

            addProductImages(tiramisu,
                "https://images.freepik.com/free-photo/tiramisu-in-glass_1134-7890.jpg",
                "https://images.freepik.com/free-photo/traditional-tiramisu-cake_1134-8901.jpg",
                "https://images.freepik.com/free-photo/homemade-tiramisu-layered_1134-9012.jpg"
            );
        }
    }

    private void generateMainCourseProducts(Business business, List<Category> categories, List<Brand> brands) {
        Category mainCategory = categories.get(2); // Main Courses
        Brand localHarvest = brands.get(1); // Local Harvest

        // Grilled Fish
        if (productRepository.findByNameAndBusinessIdAndIsDeletedFalse("Grilled Fish", business.getId()).isEmpty()) {
            Product grilledFish = new Product();
            grilledFish.setBusinessId(business.getId());
            grilledFish.setName("Grilled Fish");
            grilledFish.setDescription("Fresh grilled fish with herb butter and seasonal vegetables");
            grilledFish.setCategoryId(mainCategory.getId());
            grilledFish.setBrandId(localHarvest.getId());
            grilledFish.setStatus(ProductStatus.ACTIVE);
            grilledFish.setMainImageUrl("https://images.freepik.com/free-photo/grilled-fish-fillet-with-herbs_1134-1234.jpg");

            grilledFish = productRepository.save(grilledFish);

            addProductSizes(grilledFish,
                new SizeData("Regular", new BigDecimal("12.50")),
                new SizeData("Large", new BigDecimal("16.00"))
            );

            addPromotion(grilledFish, PromotionType.PERCENTAGE, new BigDecimal("12"));

            addProductImages(grilledFish,
                "https://images.freepik.com/free-photo/baked-fish-with-vegetables_1134-2345.jpg",
                "https://images.freepik.com/free-photo/grilled-salmon-lemon-dill_1134-3456.jpg",
                "https://images.freepik.com/free-photo/seafood-dinner-fresh-fish_1134-4567.jpg"
            );
        }

        // Pad Thai
        if (productRepository.findByNameAndBusinessIdAndIsDeletedFalse("Pad Thai", business.getId()).isEmpty()) {
            Product padThai = new Product();
            padThai.setBusinessId(business.getId());
            padThai.setName("Pad Thai");
            padThai.setDescription("Traditional Thai stir-fried noodles with shrimp and peanuts");
            padThai.setCategoryId(mainCategory.getId());
            padThai.setBrandId(localHarvest.getId());
            padThai.setStatus(ProductStatus.ACTIVE);
            padThai.setMainImageUrl("https://images.freepik.com/free-photo/thai-pad-thai-noodles-shrimp_1134-5678.jpg");

            padThai = productRepository.save(padThai);

            addProductSizes(padThai,
                new SizeData("Regular", new BigDecimal("8.50")),
                new SizeData("Large", new BigDecimal("11.00"))
            );

            addProductImages(padThai,
                "https://images.freepik.com/free-photo/stir-fried-noodles-wok-pan_1134-6789.jpg",
                "https://images.freepik.com/free-photo/asian-stir-fry-rice-noodles_1134-7890.jpg",
                "https://images.freepik.com/free-photo/pad-thai-with-shrimp-peanuts_1134-8901.jpg"
            );
        }

        // Beef Stew
        if (productRepository.findByNameAndBusinessIdAndIsDeletedFalse("Beef Stew", business.getId()).isEmpty()) {
            Product beefStew = new Product();
            beefStew.setBusinessId(business.getId());
            beefStew.setName("Beef Stew");
            beefStew.setDescription("Tender beef stew with potatoes, carrots and rich gravy");
            beefStew.setCategoryId(mainCategory.getId());
            beefStew.setBrandId(localHarvest.getId());
            beefStew.setStatus(ProductStatus.ACTIVE);
            beefStew.setMainImageUrl("https://images.freepik.com/free-photo/hearty-beef-stew-comfort-food_1134-9012.jpg");

            beefStew = productRepository.save(beefStew);

            addProductSizes(beefStew,
                new SizeData("Regular", new BigDecimal("10.00")),
                new SizeData("Large", new BigDecimal("13.00"))
            );

            addPromotion(beefStew, PromotionType.FIXED_AMOUNT, new BigDecimal("1.50"));

            addProductImages(beefStew,
                "https://images.freepik.com/free-photo/slow-cooked-beef-stew-vegetables_1134-0123.jpg",
                "https://images.freepik.com/free-photo/braised-beef-with-potatoes-carrots_1134-1234.jpg",
                "https://images.freepik.com/free-photo/beef-bourguignon-stew_1134-2345.jpg"
            );
        }
    }

    private void addProductSizes(Product product, SizeData... sizes) {
        for (SizeData size : sizes) {
            if (productSizeRepository.findByProductIdAndNameAndIsDeletedFalse(product.getId(), size.name).isEmpty()) {
                ProductSize productSize = new ProductSize();
                productSize.setProductId(product.getId());
                productSize.setName(size.name);
                productSize.setPrice(size.price);
                productSize.setIsAvailable(true);
                productSizeRepository.save(productSize);
            }
        }
    }

    private void addPromotion(Product product, PromotionType type, BigDecimal value) {
        product.setPromotionType(type);
        product.setPromotionValue(value);
        product.setPromotionFromDate(LocalDateTime.now().truncatedTo(ChronoUnit.HOURS));
        product.setPromotionToDate(LocalDateTime.now().plusDays(30).truncatedTo(ChronoUnit.HOURS));
        productRepository.save(product);
    }

    private void addProductImages(Product product, String... imageUrls) {
        for (int i = 0; i < imageUrls.length; i++) {
            if (productImageRepository.findByProductIdAndImageUrlAndIsDeletedFalse(product.getId(), imageUrls[i]).isEmpty()) {
                ProductImage image = new ProductImage();
                image.setProductId(product.getId());
                image.setImageUrl(imageUrls[i]);
                image.setAltText(product.getName() + " - Image " + (i + 1));
                image.setDisplayOrder(i + 1);
                image.setIsPrimary(i == 0);
                productImageRepository.save(image);
            }
        }
    }

    /**
     * Get Freepik image URL based on category
     */
    private String getFreepikImageForCategory(String categoryName) {
        return switch (categoryName) {
            case "Beverages" -> "https://images.freepik.com/free-photo/variety-hot-cold-beverages-flat-lay_1134-3456.jpg";
            case "Desserts" -> "https://images.freepik.com/free-photo/assorted-homemade-desserts-table_1134-4567.jpg";
            case "Main Courses" -> "https://images.freepik.com/free-photo/various-cooked-main-course-dishes_1134-5678.jpg";
            case "Appetizers" -> "https://images.freepik.com/free-photo/appetizer-platter-mixed-starters_1134-6789.jpg";
            case "Salads" -> "https://images.freepik.com/free-photo/fresh-salad-mixed-vegetables-bowl_1134-7890.jpg";
            default -> "https://images.freepik.com/free-photo/food-table-restaurant-setting_1134-8901.jpg";
        };
    }

    /**
     * Get Freepik image URL based on brand
     */
    private String getFreepikImageForBrand(String brandName) {
        return switch (brandName) {
            case "Premium Roast" -> "https://images.freepik.com/free-photo/premium-roasted-coffee-beans-close-up_1134-2345.jpg";
            case "Local Harvest" -> "https://images.freepik.com/free-photo/local-farm-fresh-produce-market_1134-3456.jpg";
            case "Artisan Blend" -> "https://images.freepik.com/free-photo/artisan-food-preparation-kitchen_1134-4567.jpg";
            case "Heritage Taste" -> "https://images.freepik.com/free-photo/traditional-cuisine-heritage-food_1134-5678.jpg";
            default -> "https://images.freepik.com/free-photo/brand-identity-business-concept_1134-6789.jpg";
        };
    }

    /**
     * Helper class for product size data
     */
    private static class SizeData {
        String name;
        BigDecimal price;

        SizeData(String name, BigDecimal price) {
            this.name = name;
            this.price = price;
        }
    }
}
