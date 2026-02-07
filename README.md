# 🧺 Washing Unit (Pvt) Ltd - Enterprise Management System

![Project Status](https://img.shields.io/badge/Status-Active-success)
![Java](https://img.shields.io/badge/Java-21-orange)
![PHP](https://img.shields.io/badge/PHP-8.2-blue)
![MySQL](https://img.shields.io/badge/Database-MySQL-lightgrey)

## 📖 Overview

This is a **Hybrid Enterprise Resource Planning (ERP) System** designed for **Washing Unit (Pvt) Ltd**. It seamlessly integrates a **Java Desktop Application** for internal operations (Inventory & Finance) with a **PHP Web Portal** for external stakeholders (Suppliers & Customers).

The system automates the entire supply chain workflow:
1.  **Suppliers** receive Purchase Orders (POs) and upload invoices via the Web Portal.
2.  **Internal Staff** manage stock, approve payments, and track usage via the Java Desktop App.
3.  **Customers** place service orders and track status via the Customer Web Dashboard.

---

## 🏗️ Technology Stack

### **Desktop Application (Internal Admin)**
* **Language:** Java (JDK 21)
* **GUI Framework:** Java Swing (Modern UI with FlatLaf/Custom Styling)
* **Database Connectivity:** JDBC (MySQL Connector)
* **Key Libraries:** * `JCalendar` (Date Pickers)
    * `Google API Client` (Drive Integration for Receipts)
    * `JasperReports` (PDF Generation)

### **Web Portal (Supplier & Customer)**
* **Backend:** PHP (Native PDO for Security)
* **Frontend:** HTML5, CSS3 (Glassmorphism UI), JavaScript (ES6+ Fetch API)
* **Database:** MySQL / MariaDB (Shared with Desktop App)
* **Server:** Apache (XAMPP)

---

## ✨ Key Features

### 🖥️ Desktop App (Java)
* **Inventory Management:** Track raw materials (Dye, Chemicals) with "IN/OUT" transaction logs.
* **Stock History:** Visual tables showing usage history and operator costs.
* **Payment Verification:** Verify supplier invoices and mark them as "Success" or "Failed".
* **Automated Sync:** Updates order status (e.g., from "Pending" to "Completed") instantly across the database.

### 🌐 Web Portal (PHP)
* **Supplier Dashboard:** * View new Purchase Orders (POs).
    * Upload PDF Invoices.
    * Track Payment Status (Pending vs. Settled).
* **Customer Dashboard:** * Place new service requests.
    * Track order progress (Processing -> Completed).
    * View payment dues.
* **Security:** * SQL Injection protection via Prepared Statements.
    * Session-based Authentication.

---

## ⚙️ Installation & Setup (A to Z)

### 1️⃣ Database Setup
1.  Install **XAMPP** and start **Apache** and **MySQL**.
2.  Open **phpMyAdmin** (`http://localhost/phpmyadmin`).
3.  Create a database named `production_db`.
4.  Import the SQL file located in:  
    `database/production_db.sql` (or `production_db (11).sql`)

### 2️⃣ Web Portal Setup
1.  Copy the `FINAL_PROJECT` folder to your XAMPP `htdocs` directory:
    * Path: `C:\xampp\htdocs\AFinal\`
2.  Configure the database connection:
    * Edit `php/db_config.php`:
    ```php
    $host = 'localhost';
    $db   = 'production_db';
    $user = 'root';
    $pass = ''; // Default XAMPP password
    ```
3.  Access the site: `http://localhost/AFinal/`

4.  Site Repo link <a href="https://github.com/NimnaOfficial/LankaWashing">Click Here</a>

### 3️⃣ Java Desktop App Setup
1.  Open the project in **IntelliJ IDEA** or **NetBeans**.
2.  **Add Dependencies:** Ensure the following JARs are in your library path:
    * `mysql-connector-j-9.x.jar`
    * `jcalendar-1.4.jar`
    * `google-api-client` JARs (if using Drive features)
3.  **Configure Credentials:**
    * Since `reciept.json` and `tokens/` are ignored by Git for security, you must add your own **Google OAuth Credentials** file to the project root if you plan to use the Drive Upload feature.
4.  Run `main.Main` class.

---

## 📸 Screenshots

| **Supplier Dashboard** | **Java Inventory Panel** |
|:---:|:---:|
| ![Supplier Dash](https://i.ibb.co/gbNnPt51/Screenshot-2026-02-07-211018.png) | ![Java App](https://i.ibb.co/DgVcF6t5/Screenshot-2026-02-07-214121.png) |
| *Real-time stats and invoice uploads* | *Stock tracking and resource management* |

---

## 🔒 Security & Best Practices
* **Secrets Management:** API Keys and OAuth Tokens are **excluded** from version control via `.gitignore`.
* **Data Integrity:** Transaction blocks (`conn.setAutoCommit(false)`) are used for critical updates (e.g., Accepting POs) to prevent data mismatches.
* **Validation:** All user inputs (PHP & Java) are validated before processing to prevent SQL Injection and XSS.

---

## 🚀 Future Roadmap
* [ ] Add Email Notifications for Order Status changes.
* [ ] Implement Role-Based Access Control (RBAC) for Java Admin.
* [ ] Migrate local file storage to AWS S3.

---

## 👨‍💻 Contributors
* **NimnaOfficial** - *Developer (Full Stack)*
* **imaam07**       - *Lead Developer (Full Stack)*
* **malith-pasindu** - *Developer (Full Stack)*

---

### ⚖️ License
This project is for educational purposes as part of the Software Engineering Final Project.
