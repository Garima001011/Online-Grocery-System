# 🛒 LocalGrocery – Setup Guide

Welcome to the **LocalGrocery** project! This is a Spring Boot application designed for managing local grocery operations. This guide will walk you through the steps to set up and run the project locally.

---

## 📦 Prerequisites

Make sure the following tools are installed and properly configured on your machine:

- ✅ **MySQL 9.5+**
- ☕ **Java 17+**
- 📦 **Maven** or **Gradle**
- 💡 **IntelliJ IDEA** (Recommended)

---

## ⚡ Quick Start

### 1️⃣ Start MySQL
```bash
**macOS**
`brew services start mysql
Windows

net start mysql
2️⃣ Set MySQL Root Password (if needed)
Connect to MySQL:
mysql -u root
Run the following SQL commands:

sql
ALTER USER 'root'@'localhost' IDENTIFIED BY 'admin';
FLUSH PRIVILEGES;
EXIT;
3️⃣ Load Database Seed Data
From the root directory of the project, run:
mysql -u root -padmin < seed_data.sql
When prompted, enter the password: admin

This will create the database, tables, and insert sample data.

4️⃣ Configure Application
Edit the following file:

📄 src/main/resources/application.properties

Add this configuration:

properties
spring.datasource.url=jdbc:mysql://localhost:3306/localgrocery
spring.datasource.username=root
spring.datasource.password=admin

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
5️⃣ Run the Project
💡 Using IntelliJ IDEA (Recommended)
Open the project in IntelliJ.

Locate the file: LocalGroceryApplication.java.

Click the green ▶️ Run button.

🔧 Using Mavenmvn spring-boot:run
🔧 Using Gradle./gradlew bootRun
🔐 Test Credentials
Use the following accounts to log in:

Role	Email	Password
Admin	admin@grocery.com	admin
Customer	user@grocery.com	1234
Delivery	delivery1@local.com	pass123

✅ Verify Setup
📋 Check Tablesmysql -u root -padmin -e "USE localgrocery; SHOW TABLES;"
📊 Check Sample Datamysql -u root -padmin -e "
USE localgrocery;
SELECT 'Users' AS label, COUNT(*) FROM users;
SELECT 'Products' AS label, COUNT(*) FROM products;
"
🧰 Troubleshooting
❌ MySQL Not Connecting
macOS
brew services restart mysql
Windows
net stop mysql
net start mysql
🔒 Access Denied Error
Reset root password:

Connect as root:
sudo mysql -u root
Then run:


ALTER USER 'root'@'localhost' IDENTIFIED BY 'admin';
FLUSH PRIVILEGES;
🚫 Application Won’t Start?
Make sure:

✅ MySQL is running

✅ Credentials in application.properties are correct

✅ Port 3306 is not blocked

✅ Java version is 17+

🗃️ Database Info
Database Name: localgrocery

Tables: 9
(users, stores, products, orders, etc.)

📦 Sample Data
4 users

9 stores

34 products

3 orders

🌐 API Base URL
Once running, access the app at:
http://localhost:8080
📝 Notes
This project is configured for local development

Built with Spring Boot + MySQL

Seed data is required for login and testing

