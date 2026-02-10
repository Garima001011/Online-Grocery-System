-- ============================================
-- LocalGrocery Database Seed Data
-- Complete setup including all tables and data
-- ============================================

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS localgrocery;
USE localgrocery;

-- ============================================
-- 1. TABLES CREATION
-- ============================================

-- Drop tables if they exist (in correct order due to foreign key constraints)
DROP TABLE IF EXISTS notification_logs;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS stores;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS sales_reports;
DROP VIEW IF EXISTS admin_returns_view;

-- Create users table
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    name VARCHAR(255)
);

-- Create stores table
CREATE TABLE stores (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(500)
);

-- Create categories table
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL
);

-- Create products table
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    category_id BIGINT,
    store_id BIGINT,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (store_id) REFERENCES stores(id)
);

-- Create orders table
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    delivery_address VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    card_brand VARCHAR(255),
    card_expiry VARCHAR(255),
    card_last4 VARCHAR(255),
    payment_method VARCHAR(255) NOT NULL,
    promo_code VARCHAR(255),
    subtotal DECIMAL(12,2) NOT NULL,
    tax DECIMAL(12,2) NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    delivery_user_id BIGINT,
    delivery_notes VARCHAR(500),
    assigned_at DATETIME(6),
    picked_up_at DATETIME(6),
    delivered_at DATETIME(6),
    delivery_status VARCHAR(50) DEFAULT 'PENDING',
    payment_status VARCHAR(50) DEFAULT 'PENDING',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (delivery_user_id) REFERENCES users(id)
);

-- Create order_items table
CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    price_at_purchase DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    order_id BIGINT,
    product_id BIGINT,
    return_status ENUM('NONE', 'REQUESTED', 'APPROVED', 'REJECTED', 'REFUNDED') DEFAULT 'NONE',
    return_reason TEXT,
    return_requested_at DATETIME(6),
    return_processed_at DATETIME(6),
    refund_amount DECIMAL(12,2),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Create notifications table
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(20) DEFAULT 'INFO',
    target_audience VARCHAR(20) DEFAULT 'ALL',
    scheduled_for DATETIME,
    status VARCHAR(20) DEFAULT 'DRAFT',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Create notification_logs table
CREATE TABLE notification_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    notification_id BIGINT,
    user_id BIGINT,
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    read_at DATETIME,
    FOREIGN KEY (notification_id) REFERENCES notifications(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create sales_reports table
CREATE TABLE sales_reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_date DATE NOT NULL,
    total_orders INT DEFAULT 0,
    total_revenue DECIMAL(12,2) DEFAULT 0,
    total_customers INT DEFAULT 0,
    avg_order_value DECIMAL(10,2) DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_report_date (report_date)
);

-- Create admin_returns_view
CREATE VIEW admin_returns_view AS
SELECT
    o.id AS order_id,
    o.created_at,
    u.name AS customer_name,
    u.email AS customer_email,
    oi.id AS item_id,
    p.name AS product_name,
    oi.quantity,
    oi.price_at_purchase,
    oi.return_status,
    oi.return_reason,
    oi.return_requested_at,
    oi.refund_amount,
    s.name AS store_name
FROM order_items oi
JOIN orders o ON oi.order_id = o.id
JOIN users u ON o.user_id = u.id
JOIN products p ON oi.product_id = p.id
JOIN stores s ON p.store_id = s.id
WHERE oi.return_status != 'NONE';

-- ============================================
-- 2. SEED DATA INSERTION
-- ============================================

-- Insert users
INSERT INTO users (email, password, role, name) VALUES
('admin@grocery.com', 'admin', 'ADMIN', NULL),
('user@grocery.com', '1234', 'CUSTOMER', NULL),
('delivery1@local.com', 'pass123', 'DELIVERY', NULL),
('customer@test.com', 'test123', 'CUSTOMER', NULL);

-- Insert stores
INSERT INTO stores (name, location, description, image_url) VALUES
('Sagar dai ko kirana', 'Itahari 6', NULL, NULL),
('Bhatbhateni Itahari', 'Itahari-6, Main Road', NULL, NULL),
('Sathi Kirana Pasal', 'Itahari-6, Near Sathi Petrol Pump', NULL, NULL),
('Biratnagar Fresh Mart', 'Biratnagar-10, Traffic Chowk', NULL, NULL),
('Shree Ganesh Kirana', 'Itahari-4', NULL, NULL),
('Himalaya Grocery', 'Biratnagar-12', NULL, NULL),
('New Everest Store', 'Itahari-9', NULL, NULL),
('Sajha Pasal', 'Biratnagar-8', NULL, NULL),
('Aashish Mart', 'Itahari-3', NULL, NULL);

-- Insert categories
INSERT INTO categories (name) VALUES
('Fruits'),
('Grocery'),
('Rice & Grains'),
('Daal & Pulses'),
('Spices & Masala'),
('Noodles & Snacks'),
('Oil & Ghee'),
('Dairy'),
('Beverages'),
('Cleaning'),
('Biscuits & Bakery'),
('Sugar & Salt'),
('Tea & Coffee'),
('Flour & Atta');

-- Insert products
INSERT INTO products (name, price, stock, category_id, store_id, description, image_url) VALUES
('Apple', 1.99, 48, 1, 1, NULL, NULL),
('Masoor Daal 1kg', 260, 60, 2, 1, NULL, NULL),
('Wai Wai (Pack of 5)', 130, 79, 6, 3, NULL, NULL),
('Basmati Rice 10kg', 3200, 20, 3, 2, NULL, NULL),
('Mansuli Rice 5kg', 1350, 30, 3, 3, NULL, NULL),
('Jeera Masino Rice 5kg', 1550, 25, 3, 4, NULL, NULL),
('Chiura Thin 1kg', 230, 40, 3, 5, NULL, NULL),
('Chiura Thick 1kg', 220, 35, 3, 6, NULL, NULL),
('Masoor Daal 1kg', 270, 50, 4, 2, NULL, NULL),
('Moong Daal 1kg', 330, 40, 4, 3, NULL, NULL),
('Black Daal 1kg', 290, 30, 4, 4, NULL, NULL),
('Rajma Red 1kg', 360, 25, 4, 5, NULL, NULL),
('Chana Daal 1kg', 250, 45, 4, 6, NULL, NULL),
('Mustard Oil 1L', 365, 30, 7, 2, NULL, NULL),
('Sunflower Oil 1L', 325, 25, 7, 3, NULL, NULL),
('Soybean Oil 1L', 305, 35, 7, 4, NULL, NULL),
('Cow Ghee 500ml', 560, 15, 7, 5, NULL, NULL),
('Turmeric Powder 200g', 85, 60, 5, 2, NULL, NULL),
('Cumin Powder 200g', 145, 45, 5, 3, NULL, NULL),
('Coriander Powder 200g', 100, 50, 5, 4, NULL, NULL),
('Red Chili Powder 200g', 135, 40, 5, 5, NULL, NULL),
('Garam Masala 100g', 130, 30, 5, 6, NULL, NULL),
('Meat Masala 100g', 145, 25, 5, 3, NULL, NULL),
('Wai Wai Quick Pack', 135, 80, 6, 2, NULL, NULL),
('Rara Noodles Pack', 125, 70, 6, 3, NULL, NULL),
('Current Noodles Pack', 130, 60, 6, 4, NULL, NULL),
('Lays Chips 60g', 60, 90, 6, 5, NULL, NULL),
('KurKure Masala', 65, 85, 6, 6, NULL, NULL),
('Haldiram Bhujia 200g', 185, 40, 6, 2, NULL, NULL),
('Apple 1kg', 260, 40, 1, 2, NULL, NULL),
('Banana 1 dozen', 180, 45, 1, 3, NULL, NULL),
('Orange 1kg', 220, 35, 1, 4, NULL, NULL),
('Pomegranate 1kg', 420, 20, 1, 5, NULL, NULL),
('Grapes 500g', 240, 25, 1, 6, NULL, NULL);

-- Insert orders (with actual timestamps from your session)
INSERT INTO orders (id, created_at, delivery_address, status, user_id, payment_method, subtotal, tax, total,
                    delivery_user_id, assigned_at, picked_up_at, delivered_at, delivery_status, payment_status) VALUES
(1, '2026-01-01 22:47:57.028188', 'Itahari-6, Near Sathi Petrol Pump', 'DELIVERED', 2, 'COD', 0.00, 0.00, 0.00,
 3, '2026-01-01 23:29:52.951529', '2026-01-01 23:30:33.248970', '2026-01-01 23:30:33.317189', 'DELIVERED', 'PAID'),
(2, '2026-01-01 22:52:18.713492', 'Sathi Pertrol Pump', 'PLACED', 4, 'COD', 0.00, 0.00, 0.00,
 NULL, NULL, NULL, NULL, 'PENDING', 'PENDING'),
(3, NOW(), 'Biratnagar-10', 'PLACED', 2, 'COD', 500.00, 65.00, 565.00,
 NULL, NULL, NULL, NULL, 'PENDING', 'PENDING');

-- Reset auto increment for orders
ALTER TABLE orders AUTO_INCREMENT = 4;

-- Insert order_items
INSERT INTO order_items (price_at_purchase, quantity, order_id, product_id, return_status) VALUES
(1.99, 2, 1, 1, 'NONE'),
(130, 1, 1, 3, 'NONE'),
(1.99, 31, 2, 1, 'NONE');

-- ============================================
-- 3. SETUP SCRIPT FOR QUICK START
-- ============================================

-- Display summary of seeded data
SELECT 'Database Setup Complete!' AS message;

SELECT
    (SELECT COUNT(*) FROM users) AS total_users,
    (SELECT COUNT(*) FROM stores) AS total_stores,
    (SELECT COUNT(*) FROM categories) AS total_categories,
    (SELECT COUNT(*) FROM products) AS total_products,
    (SELECT COUNT(*) FROM orders) AS total_orders;

-- Show user credentials for testing
SELECT '=== TEST CREDENTIALS ===' AS info;
SELECT email, password, role FROM users;

-- Show available endpoints (example)
SELECT '=== SAMPLE API ENDPOINTS ===' AS info;
SELECT
    'POST /api/auth/login' AS endpoint,
    'User authentication' AS description
UNION ALL
SELECT
    'GET /api/products',
    'List all products'
UNION ALL
SELECT
    'GET /api/stores',
    'List all stores'
UNION ALL
SELECT
    'POST /api/orders',
    'Create new order';

-- ============================================
-- 4. OPTIONAL: ADDITIONAL SAMPLE DATA
-- ============================================

-- Add some sample notifications
INSERT INTO notifications (title, message, notification_type, target_audience, status) VALUES
('Welcome to LocalGrocery!', 'Get 20% off on your first order. Use code: WELCOME20', 'OFFER', 'CUSTOMERS', 'SENT'),
('New Store Added', 'Bhatbhateni Itahari is now available for delivery', 'INFO', 'ALL', 'SENT'),
('Festival Sale', 'Diwali special discounts on all groceries', 'DEAL', 'ALL', 'SCHEDULED');

-- Add a sample sales report
INSERT INTO sales_reports (report_date, total_orders, total_revenue, total_customers, avg_order_value) VALUES
(CURDATE() - INTERVAL 1 DAY, 15, 12500.50, 8, 833.37),
(CURDATE(), 8, 8560.25, 5, 1070.03);

-- ============================================
-- 5. DATABASE CONFIGURATION
-- ============================================

-- Set root password (as you did in your session)
-- Note: This needs to be run separately in MySQL shell
-- ALTER USER 'root'@'localhost' IDENTIFIED BY 'admin';
-- FLUSH PRIVILEGES;

-- Show final configuration
SELECT '=== DATABASE READY ===' AS status;
SELECT
    @@version AS mysql_version,
    DATABASE() AS current_database,
    NOW() AS seed_executed_at;
