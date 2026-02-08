# 📘 System Documentation: Washing Unit (Pvt) Ltd
**Enterprise Resource Planning (ERP) System**

| **Version** | **Author** | **Date** |
| :--- | :--- | :--- |
| 1.0.0 | NimnaOfficial | February 08, 2026 |

---

## 1. 📝 Introduction
### 1.1 Purpose
The **Washing Unit Enterprise System** is a hybrid software solution designed to streamline the operational workflows of a large-scale industrial washing and dyeing facility. It bridges the gap between internal factory operations and external stakeholders (Suppliers and Customers) through a synchronized ecosystem.

### 1.2 Scope
* **Internal Operations:** Managed via a high-performance **Java Desktop Application** (Inventory, Financials, Admin).
* **External Operations:** Managed via a responsive **PHP Web Portal** (Supplier POs, Customer Orders, Invoices).
* **Data Synchronization:** Real-time data consistency between Desktop and Web platforms using a shared MySQL database.

### 1.3 User Roles
1.  **Admin/Staff (Internal):** Uses the Desktop App to manage stock, verify payments, and oversee production.
2.  **Supplier (External):** Uses the Web Portal to receive Purchase Orders (POs) and submit invoices.
3.  **Customer (External):** Uses the Web Portal to place service requests and track order status.

---

## 2. 🏗️ System Architecture
The system follows a **Hybrid Client-Server Architecture**.

* **Database Layer (The Core):** A centralized **MySQL/MariaDB** database hosted on a local server (e.g., XAMPP) or cloud VPS.
* **Desktop Client (Java):** Connects directly to the database via **JDBC** for high-speed internal processing.
* **Web Client (PHP):** Connects to the database via **PDO** to provide a secure, accessible interface for remote users.

---

## 3. 💻 Technology Stack

| Component | Technology | Usage |
| :--- | :--- | :--- |
| **Backend Logic** | **Java (JDK 21)** | Core logic for the Desktop Application (Inventory, Payment Verification). |
| **Web Backend** | **PHP 8.2** | Server-side scripting for the Web Portal, API Endpoints. |
| **Frontend UI** | **HTML5, CSS3, JS** | Responsive "Glassmorphism" UI for dashboards. |
| **GUI Framework** | **Java Swing** | User Interface for the Desktop Application. |
| **Database** | **MySQL / MariaDB** | Relational database storage. |
| **APIs** | **Google Drive API** | Storing and retrieving receipt images securely. |
| **Email Service** | **Formspree** | Handling contact form submissions. |

---

## 4. 📦 Key Features & Modules

### 4.1 🌐 Web Portal (Customer & Supplier)

#### **A. Customer Module**
* **Dashboard:** View real-time stats: Total Orders, Pending Orders, Completed Orders, and Payment Dues.
* **Order Management:**
    * View all orders with status indicators (Pending, Processing, Completed).
    * **Edit Requests:** Customers can modify product details or quantity *only* if the status is "Pending".
    * **Cancel Requests:** Allows cancellation of pending orders.
* **Security:** Session-based authentication ensures customers only see their own data.

#### **B. Supplier Module**
* **Dashboard:** Track "New POs" vs. "Completed POs" and financial status.
* **Purchase Orders (PO):** Suppliers can view digital POs issued by the factory.
* **Invoice Submission:** Upload PDF invoices directly to the system.

### 4.2 🖥️ Desktop Application (Internal)

#### **A. Payment Verification Center**
* **Function:** Admin reviews payments submitted by customers.
* **Workflow:**
    1.  Admin selects a pending payment.
    2.  System retrieves the proof (slip/screenshot) via Google Drive link.
    3.  Admin clicks **"Mark Success"**.
    4.  **Auto-Sync:** The system updates the `payments` table AND automatically marks the linked `orders` as "Completed".

#### **B. Inventory Management**
* Track raw materials (Dye, Fabric, Chemicals).
* Record "IN" (Purchase) and "OUT" (Production) transactions.
* Generate low-stock alerts.

---

## 5. 🗄️ Database Design
The system relies on a relational database schema (`production_db`).

### **Key Tables**
1.  **`customerrequest`**: Stores initial service requests (Pending state).
2.  **`orders`**: Stores confirmed orders (Processing/Completed state). Linked to requests.
3.  **`payments`**: Records transaction details, amounts, and statuses (`Success`, `Failed`, `Pending`).
4.  **`purchaseorder`**: Orders sent to suppliers for raw materials.
5.  **`invoices`**: Bills submitted by suppliers.

---

## 6. 🛠️ Installation & Setup Guide

### **Prerequisites**
* **XAMPP** (or WAMP/MAMP) installed.
* **Java JDK 21** installed.
* **IntelliJ IDEA** or **NetBeans**.
* **Git** (for version control).

### **Step 1: Database Setup**
1.  Start **Apache** and **MySQL** in XAMPP control panel.
2.  Go to `http://localhost/phpmyadmin`.
3.  Create a database named `production_db`.
4.  Import the SQL file: `database/production_db.sql`.

### **Step 2: Web Portal Deployment**
1.  Copy the `FINAL_PROJECT` folder to `C:\xampp\htdocs\`.
2.  Rename the folder to `AFinal` (to match the paths in your code).
3.  Verify `php/db_config.php`:
    ```php
    $host = 'localhost';
    $dbname = 'production_db';
    $username = 'root';
    $password = '';
    ```
4.  Access via browser: `http://localhost/AFinal/`.

### **Step 3: Desktop App Setup**
1.  Open the project in your Java IDE.
2.  Add the required **JAR Libraries** to your project structure:
    * `mysql-connector-j-9.x.jar` (JDBC Driver).
    * `jcalendar-1.4.jar`.
3.  Run the `main` class.

---

## 7. 🔒 Security Measures
1.  **SQL Injection Protection:**
    * All database queries use **Prepared Statements** (e.g., `WHERE requestId = ?`) to prevent malicious SQL injection.
2.  **XSS Protection:**
    * Data output in the web portal is sanitized to prevent Cross-Site Scripting.
3.  **Access Control:**
    * Strict session checks (`!isset($_SESSION['customer_id'])`) prevent unauthorized access to dashboards.
    * Backend logic verifies data ownership (e.g., a customer can only cancel *their own* orders).
4.  **Credential Safety:**
    * Sensitive files like `reciept.json` and `tokens/` are excluded from Git repositories using `.gitignore`.

---

## 8. 🐛 Troubleshooting
* **Issue:** "Completed Orders" shows 0 on Dashboard.
    * **Fix:** Ensure the database has orders with status `'Completed'`, `'Delivered'`, or `'Done'`. Pending orders do not count.
* **Issue:** Desktop App cannot connect to Database.
    * **Fix:** Ensure XAMPP MySQL is running on port **3306**. If using an online DB, note that free hosts often block remote connections.
* **Issue:** Emails not sending.
    * **Fix:** Check your Formspree activation email. You must activate the form endpoint before receiving messages.

---

*End of Documentation*
