# Web-Based PG and Hostel Management System
> **An Advanced Java Project Based Learning (PBL) Enterprise System**  
> Developed using **Java 17**, **Spring Boot 3.3.4**, **Spring Security 6**, **JJWT 0.12.6**, **Spring Data JPA**, **MySQL 8.0**, and **React 18 (Vite + Bootstrap 5)**.

---

## 📌 Executive Summary
Managing modern student residences, hostels, and paying-guest (PG) facilities requires reliable coordination between room allocations, dynamic bed occupancy tracking, recurring fee invoicing, online payments reconciliation, daily mess dining timetables, maintenance tickets, visitor passes, and night curfew leave permissions.

This system replaces obsolete manual registers with a **secure, high-concurrency, role-based full-stack platform** featuring dual dashboards (Administrator/Accountant and Student Resident), instant downloadable PDF receipts, and real-time analytical reporting.

---

## 🏗️ Architecture & Technology Stack

```
                                  [ Single-Page Application (SPA) ]
                                   React 18 + Vite + Bootstrap 5
                                   (Axios Interceptor + JWT Auth)
                                                │
                                    REST APIs   │ HTTP / JSON
                                                ▼
                             ┌─────────────────────────────────────┐
                             │    Spring Boot 3.3.4 Application    │
                             │  com.hostel.management (Java 17)   │
                             ├─────────────────────────────────────┤
                             │  Spring Security 6 (Stateless JWT)  │
                             │  JPA / Hibernate 6 (ORM & JPQL)    │
                             │  OpenPDF 2.0 (Document Engine)      │
                             └─────────────────────────────────────┘
                                                │
                                     JDBC / TCP │ Port 3306
                                                ▼
                                    [ MySQL 8.0 Database ]
                                        (hostel_db)
```

### Technology Breakdown:
- **Backend Framework:** Spring Boot `3.3.4` (JDK 17)
- **Security & RBAC:** Spring Security 6 with JJWT `0.12.6` (HMAC-SHA256, Stateless Bearer Token)
- **Persistence:** Spring Data JPA + Hibernate 6 ORM
- **Database:** MySQL 8.0 (`hostel_db`, InnoDB engine, UTF-8 unicode)
- **PDF Engine:** OpenPDF `2.0.3` (Memory-streamed receipt & audit reports)
- **Frontend Framework:** React `18.3.1` + Vite `5.4.8`
- **UI & Icons:** Bootstrap `5.3.3` + Bootstrap Icons `1.11.3`
- **HTTP Client:** Axios `1.7.7` with automated Bearer token injection and 401 session recovery
- **Routing:** React Router DOM `v6.26.2` with Protected Role-Based Layouts

---

## 📂 Project Directory Structure

```
Adv Java Project/
├── pom.xml                                   # Spring Boot Maven Parent & Dependencies
├── README.md                                 # Complete Technical Documentation
├── src/
│   ├── main/
│   │   ├── java/com/hostel/management/
│   │   │   ├── HostelManagementApplication.java
│   │   │   ├── config/                       # SecurityConfig, CorsConfig, DataInitializer
│   │   │   ├── controller/                   # 12 REST API Controllers
│   │   │   ├── dto/                          # Request, Response, and Dashboard DTOs
│   │   │   ├── entity/                       # 10 JPA Entities (User, Student, Room, etc.)
│   │   │   ├── enums/                        # 16 Domain Enums (Role, Status, MealType...)
│   │   │   ├── exception/                    # GlobalExceptionHandler, Custom Exceptions
│   │   │   ├── repository/                   # 11 Spring Data JPA Repositories
│   │   │   ├── security/                     # JwtService, CustomUserDetails, JwtFilter
│   │   │   └── service/                      # Interfaces & Implementations (@Transactional)
│   │   └── resources/
│   │       ├── application.properties        # MySQL, JWT, Hibernate configurations
│   │       ├── schema.sql                    # Clean MySQL 8 DDL script
│   │       └── data.sql                      # Evaluation & Demo data seed script
└── frontend/                                 # React 18 + Vite Client
    ├── package.json
    ├── vite.config.js                        # Reverse Proxy configuration to Port 8080
    ├── index.html
    └── src/
        ├── App.jsx                           # 20 Concrete Application Routes
        ├── main.jsx
        ├── index.css                         # Custom Design System & CSS Variables
        ├── api/axiosConfig.js                # Global Axios JWT Interceptor
        ├── context/AuthContext.jsx           # Global Auth State & Role Permissions
        ├── components/
        │   ├── common/                       # ProtectedRoute, StatCard, LoadingSpinner
        │   └── layout/                       # Sidebar, Top Navbar, Footer, DashboardLayout
        └── pages/
            ├── LandingPage.jsx
            ├── auth/                         # Login & Student Register
            ├── error/                        # 403 Unauthorized & 404 NotFound
            ├── admin/                        # 11 Admin Management Workspaces
            └── student/                      # 9 Student Resident Workspaces
```

---

## 🔑 Default Evaluation Credentials

| Role | Email Address | Password | Permitted Features |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin@hostel.com` | `Admin@123` | Full access: Rooms, Allocations, Bulk Billing, Notices, Mess Menu, Reports |
| **ACCOUNTANT** | `accountant@hostel.com` | `Accountant@123` | Financial Ledger, Fee Verification, Collection Reports, Dashboard |
| **STUDENT** | `rahul@hostel.com` | `Student@123` | My Room, Online Fee Payment, Lodge Complaints, Leave Passes, Mess Menu |
| **STUDENT** | `priya@hostel.com` | `Student@123` | My Room, Fee Invoices, Visitor Requests, Dining Timetable |

*(Tip: On the frontend Login page, click the **"Quick Demo Fill"** button to auto-fill Admin or Accountant credentials in 1 click!)*

---

## 🚀 Step-by-Step Local Setup & Execution

### 1. Database Setup (MySQL 8.0)
1. Verify that your MySQL service is running on `localhost:3306`:
   ```powershell
   Get-Service MySQL*
   ```
2. The application will automatically create `hostel_db` on first run via `createDatabaseIfNotExist=true` in `application.properties`.
3. If you wish to inspect or execute the DDL script manually, open `src/main/resources/schema.sql` in MySQL Workbench or run:
   ```bash
   mysql -u root -p < src/main/resources/schema.sql
   mysql -u root -p < src/main/resources/data.sql
   ```

### 2. Launch Spring Boot Backend
From the root directory:
```bash
# Using Maven wrapper or installed Maven
mvn spring-boot:run
```
*Or open the project in IntelliJ IDEA, Eclipse, or VS Code and run `HostelManagementApplication.java`.*
- The backend will start at: `http://localhost:8080`
- `DataInitializer` will automatically seed default accounts and the 7-day mess timetable.

### 3. Launch React Frontend
Open a new terminal window:
```bash
cd frontend
npm install
npm run dev
```
- Open browser at: `http://localhost:3000`

---

## 📡 Core API Endpoints Catalog

### Authentication (`/api/auth`)
- `POST /api/auth/register` - Register a new resident user account.
- `POST /api/auth/login` - Authenticate with email/password and obtain JWT Bearer token.
- `GET /api/auth/me` - Fetch currently logged-in user profile from JWT context.

### Dashboard Cockpit (`/api/dashboard`)
- `GET /api/dashboard/admin` - Executive KPIs: occupancy rate, billing sums, monthly collections, recent complaints.
- `GET /api/dashboard/student` - Personalized cockpit: allocated room, dues balance, today's 4-meal menu, active notices.

### Rooms & Allocations (`/api/rooms`, `/api/allocations`)
- `GET /api/rooms` - Filter rooms by status, sharing type, floor, and block.
- `POST /api/rooms` - Add new room with capacity and monthly rent.
- `GET /api/rooms/available` - List rooms having vacant beds (`occupied < capacity`).
- `POST /api/allocations/request` - Student applies for a room.
- `POST /api/allocations/approve` - Warden approves request; atomically increments bed occupancy.
- `POST /api/allocations/reject` - Warden rejects request with remarks.
- `POST /api/allocations/vacate` - Student departs; atomically decrements bed occupancy.

### Billing & Payments (`/api/fees`, `/api/payments`)
- `GET /api/fees` - Paginated invoices list with month and status filters.
- `POST /api/fees/bulk` - 1-Click bulk monthly invoice generator for all active residents.
- `GET /api/fees/my` - Student views personal outstanding invoices.
- `POST /api/payments` - Process fee payment (UPI, Debit Card, Credit Card, NetBanking).
- `GET /api/payments/transaction/{txnId}` - Look up transaction audit record.

### Maintenance & Welfare (`/api/complaints`, `/api/visitors`, `/api/leaves`)
- `POST /api/complaints` - Lodge room repair ticket.
- `PUT /api/complaints/{id}/status` - Update ticket lifecycle (`PENDING -> IN_PROGRESS -> RESOLVED`).
- `POST /api/visitors/request` - Pre-register visitor pass.
- `POST /api/visitors/approve` - Gate security approval.
- `POST /api/visitors/complete` - Record visitor departure timestamp.
- `POST /api/leaves/apply` - Student applies for out-of-campus leave.
- `POST /api/leaves/approve` - Warden grants leave pass.

### Dining & Announcements (`/api/mess-menu`, `/api/notices`)
- `GET /api/mess-menu/today` - Today's live breakfast, lunch, snacks, and dinner.
- `GET /api/mess-menu/weekly` - Full 7-day culinary timetable grid (Monday to Sunday).
- `POST /api/mess-menu/upsert` - Admin adds or updates meal items with timing.
- `GET /api/notices/active` - Active bulletins sorted by priority.
- `POST /api/notices` - Broadcast official announcement.

### PDF Invoicing & Reports (`/api/reports`)
- `GET /api/reports/fee-receipt/{paymentId}` - Download tamper-evident official PDF fee receipt.
- `GET /api/reports/financial/monthly?month=YYYY-MM` - Download monthly financial collection spreadsheet PDF.
- `GET /api/reports/occupancy` - Download real-time room and bed capacity audit PDF.

---

## 🎓 Ultimate Lab PBL Viva Voce Mastery Guide

#### Q1: What makes Spring Boot 3 different from Spring Boot 2, and what are the Java 17 baseline implications?
**Answer:** Spring Boot 3 requires Java 17 as the absolute minimum baseline and has completely migrated from `javax.*` to `jakarta.*` namespace (Jakarta EE 9/10). In Java 17, features like Text Blocks, Records, Pattern Matching, and Sealed Classes improve code maintainability. Hibernate 6 has also rewritten its query translation engine (SQM - Semantic Query Model), making JPQL queries significantly faster and strictly typed.

#### Q2: How does JJWT 0.12.x differ from older deprecated versions?
**Answer:** In older versions, JJWT relied on `Jwts.parser().setSigningKey(key).parseClaimsJws(token)`. In modern JJWT 0.12.x, parser builders are strictly immutable: `Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()`. Signing uses `Jwts.builder().signWith(secretKey).compact()`, eliminating insecure string keys and enforcing cryptographic key specifications.

#### Q3: Why is `@Transactional` critical during Room Allocation and Fee Payments?
**Answer:** In room allocation, multiple operations occur: (1) creating the `RoomAllocation` record, (2) validating available bed space, and (3) incrementing `room.occupied` by 1. If step 3 fails, without `@Transactional`, the database would store an allocation without incrementing the bed count, causing double-allocation bugs. `@Transactional` ensures ACID compliance: either all statements commit atomically or rollback completely on runtime exceptions.

#### Q4: What is the N+1 select problem in Hibernate, and how does this project avoid it?
**Answer:** The N+1 problem occurs when fetching an entity with a `@ManyToOne` or `@OneToMany` relationship results in 1 query for the parent entity plus N subsequent queries for each child record. In this project, we prevent N+1 queries by using JPQL `JOIN` queries (`SELECT f FROM Fee f JOIN f.student s JOIN s.user u`) and configuring `spring.jpa.open-in-view=false` to prevent hidden lazy queries during JSON serialization.

#### Q5: Explain the Axios Interceptor flow in our React frontend.
**Answer:** We created an Axios instance in `axiosConfig.js`. The **Request Interceptor** intercepts every outgoing HTTP request, retrieves the JWT token from `localStorage`, and appends `headers.Authorization = 'Bearer ' + token`. The **Response Interceptor** listens for server responses: if an API returns `401 Unauthorized` (e.g. expired token), the interceptor clears local credentials and smoothly redirects the user to `/login?expired=true`.

#### Q6: Why did you use `BigDecimal` for all monetary amounts instead of `double` or `float`?
**Answer:** `float` and `double` use IEEE 754 floating-point binary representation which cannot accurately represent base-10 decimals (e.g., `0.1 + 0.2 = 0.30000000000000004`). In financial billing, rounding errors accumulate into noticeable discrepancy. `BigDecimal` provides arbitrary-precision signed decimal numbers, guaranteeing exact financial calculations.

---

## 📜 Project Sign-Off
This project satisfies all requirements for Advanced Java Project Based Learning (PBL):
- **12 Production Backend Modules** with validation, security, and exception handling.
- **20 Complete React Components** with zero dead links and verified Vite build.
- **Database Normalization** up to 3NF with foreign keys and composite constraints.
- **OpenPDF Integration** for tamper-evident digital receipts and reports.
