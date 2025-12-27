package com.grocery.localgrocery;

import com.grocery.localgrocery.entity.*;
import com.grocery.localgrocery.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    public DataSeeder(CategoryRepository categoryRepository,
                      ProductRepository productRepository,
                      StoreRepository storeRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
    }

    @Override
    public void run(String... args) {
        // Seed only if empty (so your DB data isn’t overwritten)
        if (productRepository.count() > 0) return;

        // Stores (2–3 local)
        Store bb = storeRepository.save(new Store("Bhatbhateni Supermarket", "Kathmandu"));
        Store bigmart = storeRepository.save(new Store("Big Mart", "Kathmandu/Lalitpur"));
        Store salesberry = storeRepository.save(new Store("SalesBerry", "Kathmandu"));

        // Categories
        Category riceFlour = categoryRepository.save(new Category("Rice & Flour"));
        Category pulses = categoryRepository.save(new Category("Daal & Pulses"));
        Category spices = categoryRepository.save(new Category("Spices & Masala"));
        Category oils = categoryRepository.save(new Category("Oil & Ghee"));
        Category teaCoffee = categoryRepository.save(new Category("Tea & Coffee"));
        Category dairy = categoryRepository.save(new Category("Dairy"));
        Category snacks = categoryRepository.save(new Category("Snacks"));
        Category noodles = categoryRepository.save(new Category("Noodles & Pasta"));
        Category beverages = categoryRepository.save(new Category("Beverages"));
        Category household = categoryRepository.save(new Category("Household"));
        Category personal = categoryRepository.save(new Category("Personal Care"));
        Category bakery = categoryRepository.save(new Category("Bakery"));

        // Helper
        java.util.function.BiFunction<Double, Integer, Integer> stock = (p, s) -> s;
        add("Sona Masoori Rice 5kg", 1250, 40, riceFlour, bb);
        add("Basmati Rice 5kg", 1850, 30, riceFlour, bb);
        add("Jeera Masino Rice 5kg", 1650, 35, riceFlour, salesberry);
        add("Wheat Flour (Atta) 5kg", 650, 50, riceFlour, bigmart);
        add("Maida 1kg", 120, 80, riceFlour, bigmart);
        add("Besan (Gram Flour) 1kg", 190, 60, riceFlour, salesberry);

        add("Masoor Daal 1kg", 220, 70, pulses, bb);
        add("Moong Daal 1kg", 240, 60, pulses, salesberry);
        add("Chana Daal 1kg", 210, 55, pulses, bigmart);
        add("Rajma 1kg", 280, 40, pulses, bb);
        add("Chickpeas (Chana) 1kg", 230, 45, pulses, salesberry);
        add("Black Lentil (Kalo Daal) 1kg", 260, 35, pulses, bigmart);

        add("Turmeric Powder 200g", 90, 120, spices, bb);
        add("Cumin (Jeera) 200g", 130, 90, spices, salesberry);
        add("Coriander Powder 200g", 80, 110, spices, bigmart);
        add("Garam Masala 100g", 140, 85, spices, bb);
        add("Timur (Sichuan Pepper) 50g", 160, 45, spices, salesberry);
        add("Jimbu 25g", 120, 40, spices, bigmart);
        add("Mustard Seeds 200g", 70, 100, spices, bb);
        add("Red Chilli Powder 200g", 110, 95, spices, salesberry);

        add("Mustard Oil 1L", 380, 50, oils, bb);
        add("Soybean Oil 1L", 320, 55, oils, bigmart);
        add("Sunflower Oil 1L", 360, 45, oils, salesberry);
        add("Ghee 500ml", 520, 35, oils, bb);

        add("Nepali Tea 250g", 210, 60, teaCoffee, salesberry);
        add("Instant Coffee 100g", 340, 40, teaCoffee, bigmart);
        add("Milk Tea Masala 50g", 120, 50, teaCoffee, bb);

        add("Milk 1L", 120, 80, dairy, bigmart);
        add("Curd (Dahi) 500g", 95, 60, dairy, bb);
        add("Paneer 200g", 190, 40, dairy, salesberry);

        add("Wai Wai Noodles (5 pack)", 110, 120, noodles, bb);
        add("Rara Noodles (5 pack)", 105, 110, noodles, bigmart);
        add("Pasta 500g", 170, 70, noodles, salesberry);

        add("Lays Classic 52g", 70, 90, snacks, bigmart);
        add("Kurkure 70g", 80, 85, snacks, bb);
        add("Haldiram Bhujia 200g", 160, 65, snacks, salesberry);
        add("Digestive Biscuits 250g", 140, 80, bakery, bigmart);
        add("Rusks 200g", 120, 60, bakery, bb);

        add("Frooti 1L", 140, 70, beverages, salesberry);
        add("Coca-Cola 1.25L", 200, 60, beverages, bb);
        add("Mineral Water 1L", 30, 200, beverages, bigmart);

        add("Dishwash Liquid 500ml", 180, 65, household, bigmart);
        add("Laundry Detergent 1kg", 260, 55, household, bb);
        add("Toilet Cleaner 500ml", 210, 45, household, salesberry);

        add("Soap 125g", 55, 150, personal, bigmart);
        add("Shampoo 180ml", 220, 60, personal, bb);
        add("Toothpaste 150g", 170, 75, personal, salesberry);

        // done
    }

    private void add(String name, double price, int stock, Category category, Store store) {
        Product p = new Product();
        p.setName(name);
        p.setPrice(price);
        p.setStock(stock);
        p.setCategory(category);
        p.setStore(store);
        productRepository.save(p);
    }
}
