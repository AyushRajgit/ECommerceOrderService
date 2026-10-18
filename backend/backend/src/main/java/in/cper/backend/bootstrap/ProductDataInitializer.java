package in.cper.backend.bootstrap;

import in.cper.backend.entity.Product;
import in.cper.backend.repository.RegisterProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProductDataInitializer implements CommandLineRunner {

    private final RegisterProductRepository productRepository;

    public ProductDataInitializer(RegisterProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        List<Product> sampleProducts = List.of(
                product("Bluetooth Wireless Headphones", 2499, "Comfortable wireless headphones with clear sound for music and calls."),
                product("Compact Bluetooth Speaker", 1799, "A portable electronic speaker for music and audio on the go."),
                product("Smartphone Fast Charger", 899, "A compact fast charger for compatible smartphones and other devices."),

                product("Stainless Steel Kitchen Bottle", 599, "A reusable insulated water bottle for the kitchen, office, or travel."),
                product("Ceramic Coffee Mug", 349, "A sturdy ceramic mug for coffee, tea, and everyday use."),
                product("Adjustable Table Lamp", 1299, "A simple adjustable lamp for a desk, bedside table, or reading corner."),

                product("Classic Cotton Shirt", 999, "A comfortable everyday fashion shirt made from soft cotton."),
                product("Canvas Tote Bag", 499, "A reusable everyday bag for shopping, books, and daily essentials."),
                product("Everyday Walking Shoes", 1599, "Lightweight casual shoes designed for comfortable daily walks."),

                product("The Little Book of Daily Habits", 399, "A practical book with simple ideas for building better everyday habits."),
                product("Mystery at Cedar Lane: A Novel", 499, "A light mystery novel for relaxed evenings and weekend reading."),
                product("Everyday Home Cooking Journal", 299, "A cooking journal for collecting recipes, notes, and meal ideas."),

                product("Daily Hydrating Face Cream", 699, "A gentle face cream for a simple daily beauty and skincare routine."),
                product("Herbal Hair Care Shampoo", 449, "A mild shampoo for everyday hair care and cleansing."),
                product("Vitamin C Glow Serum", 799, "A lightweight serum for a simple face and skin care routine."),

                product("Foldable Laptop Stand", 1299, "A portable stand that raises a laptop for a more comfortable desk setup."),
                product("1080p USB Webcam", 1899, "A clear webcam for video calls, streaming, and computer use."),
                product("Wireless Gaming Mouse", 1099, "A responsive wireless mouse for everyday computer use and gaming."),
                product("65W USB-C Charging Cable", 499, "A durable electronic charging cable for compatible phones and laptops."),
                product("Smart LED Light Strip", 899, "An electronic LED light strip with adjustable colors for a desk or room."),
                product("Portable Phone Power Bank", 1499, "A compact power bank for charging a mobile phone while travelling."),
                product("Fitness Smart Watch", 2299, "A lightweight electronic watch with everyday activity tracking features."),
                product("Universal Tablet Sleeve", 799, "A padded sleeve for carrying and protecting a tablet."),
                product("USB Desk Microphone", 1699, "A plug-and-play computer microphone for calls and recordings."),
                product("Dual-Band Wi-Fi Router", 2499, "An electronic network router for reliable wireless internet."),

                product("Nonstick Frying Pan", 899, "A practical kitchen pan for preparing everyday meals."),
                product("Glass Food Storage Set", 1199, "Reusable kitchen containers for storing prepared food and leftovers."),
                product("Wooden Serving Tray", 699, "A sturdy tray for serving drinks and snacks around the home."),
                product("Cotton Bedsheet Set", 1599, "A soft home bedding set with a fitted bedsheet and pillow covers."),
                product("Wall-Mounted Key Holder", 399, "A simple home organizer to keep keys in one convenient place."),
                product("Bamboo Cutting Board", 549, "A durable kitchen cutting board made for everyday food preparation."),
                product("Silicone Spatula Set", 499, "A heat-resistant kitchen utensil set for cooking and baking."),
                product("Double-Wall Travel Tumbler", 799, "A reusable tumbler for hot and cold drinks at home or on the go."),
                product("Decorative Cushion Cover", 599, "A soft home decor cushion cover for a sofa or reading chair."),
                product("Under-Sink Storage Organizer", 999, "A compact home organizer that adds storage beneath a sink."),

                product("Unisex Crew-Neck T-Shirt", 599, "A soft cotton fashion shirt for comfortable everyday wear."),
                product("Lightweight Denim Jacket", 1799, "A versatile casual jacket designed for everyday outfits."),
                product("Everyday Ankle Socks", 299, "A comfortable pack of everyday fashion socks for casual wear."),
                product("Classic Leather Belt", 699, "A simple fashion belt to complement casual and formal clothing."),
                product("Running Sneakers", 1899, "Lightweight running shoes for comfortable walks and workouts."),
                product("Lightweight Rain Jacket", 1499, "A packable water-resistant jacket for rainy day wear."),
                product("Printed Summer Dress", 1299, "A breathable printed dress for warm days and casual outings."),
                product("Comfortable Sandals", 899, "Easy-to-wear sandals with a comfortable sole for daily use."),
                product("Crossbody Sling Bag", 1099, "A compact fashion bag for carrying everyday essentials."),
                product("Casual Cotton Hoodie", 1199, "A soft cotton fashion hoodie for relaxed everyday outfits and layering."),

                product("Productivity Planner and Journal", 349, "A dated planner and journal for organizing daily tasks and notes."),
                product("Space Adventures: A Science Fiction Novel", 499, "An imaginative science fiction novel for evening reading."),
                product("Beginner's Guide to Python Programming", 599, "An introductory programming book with clear examples for beginners."),
                product("Mindful Living: A Short Story Collection", 449, "A collection of short stories about everyday life and mindfulness."),
                product("Indian Home Cooking Recipe Book", 549, "A recipe book with approachable ideas for everyday Indian cooking."),
                product("Garden Notes Journal", 299, "A journal for recording garden plans, observations, and seasonal notes."),
                product("Introduction to Personal Finance: A Book", 499, "A beginner-friendly book about budgeting, saving, and money basics."),
                product("Classic Mystery Story Collection", 399, "A collection of classic mystery stories for readers of all ages."),
                product("Kids' Bedtime Storybook", 349, "A colorful storybook with gentle bedtime stories for young readers."),
                product("Travel Writing: An Illustrated Book", 699, "An illustrated book of travel writing and stories from around the world."),

                product("SPF 50 Daily Sunscreen", 599, "A lightweight sunscreen for daily face and skin care."),
                product("Aloe Vera Body Lotion", 449, "A moisturizing body lotion for a simple daily skin care routine."),
                product("Rose Water Face Toner", 399, "A gentle face toner for a refreshing beauty and skincare routine."),
                product("Moisturizing Lip Balm", 199, "A pocket-sized beauty balm to keep lips soft and moisturized."),
                product("Strengthening Hair Oil", 349, "A nourishing hair care oil for a relaxing scalp massage."),
                product("Gentle Foaming Face Cleanser", 499, "A mild cleanser for everyday face and skin care."),
                product("Shea Butter Hand Cream", 299, "A rich beauty hand cream for keeping hands soft throughout the day."),
                product("Reusable Makeup Brush Set", 699, "A washable beauty brush set for applying everyday makeup."),
                product("Vitamin E Skin Care Kit", 899, "A simple beauty kit with daily skin care essentials."),
                product("Hydrating Under-Eye Gel", 549, "A cooling gel for a gentle face and under-eye care routine.")
        );

        Set<String> existingNames = productRepository.findAll().stream()
                .map(Product::getProductName)
                .filter(name -> name != null)
                .map(name -> name.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        List<Product> missingProducts = sampleProducts.stream()
                .filter(product -> !existingNames.contains(product.getProductName().toLowerCase(Locale.ROOT)))
                .toList();

        if (!missingProducts.isEmpty()) {
            productRepository.saveAll(missingProducts);
        }
    }

    private Product product(String name, long price, String description) {
        Product product = new Product();
        product.setProductName(name);
        product.setProductPrice(BigDecimal.valueOf(price));
        product.setAvailableQuantity(25);
        product.setProductDescription(description);
        product.setMerchantDetail("Shopnest Marketplace");
        return product;
    }
}
