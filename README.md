# Blood Bank Inventory & Donor Eligibility Tracker
A simple Spring Boot &amp; MySQL web application to track blood bank unit inventory, enforce donor eligibility rules, and manage unit expiration dates.
A lightweight Spring Boot application built with MySQL and vanilla JavaScript to streamline blood inventory management and enforce eligibility business rules[cite: 1, 2].

## 📌 Features
- **Donor Registration & Eligibility Checks:** Ensures a minimum gap (90 days) between consecutive donations[cite: 2].
- **Inventory Management:** Automatically logs blood collections and calculates a 42-day expiration date[cite: 2].
- **Unit Issuance:** Automatically excludes expired units or units expiring within 7 days when issuing stock[cite: 2].
- **Stock Tracking & Alerts:** View current stock counts grouped by blood group and list units nearing expiration[cite: 2].

## 🛠️ Tech Stack
- **Backend:** Java, Spring Boot, Spring Data JPA[cite: 1, 2]
- **Database:** MySQL[cite: 1, 2]
- **Frontend:** HTML, CSS, JavaScript

## 📂 Project Structure
```text
src/main/
├── java/org/example/bloodbank/
│   ├── Controller/      # REST API endpoints
│   ├── Model/           # JPA Entities (Donor, BloodUnit)
│   ├── Repository/      # Spring Data JPA Interfaces
│   ├── Service/         # Core business logic & eligibility rules
│   └── BloodBankApplication.java
└── resources/
    ├── static/          # Web frontend (html, css, js)
    └── application.properties
