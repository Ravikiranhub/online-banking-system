# BankSphere

BankSphere is a Java 21, Spring Boot 3, MySQL, and React online banking simulation. It is educational software only: deposits, withdrawals, and transfers use demo money and never connect to real bank accounts or payment rails.

## What Is Included

- JWT login and registration with BCrypt password hashing.
- Customer dashboard, accounts, transfers, deposits, withdrawals, transaction history, statements, profile, notifications, and logout.
- Admin dashboard for customers, demo accounts, account status changes, transactions, statistics, and audit logs.
- Transactional transfer logic with server-side balance calculations, account ownership checks, pessimistic account locks, and duplicate transfer prevention.
- Swagger/OpenAPI at `http://localhost:8080/swagger-ui.html`.
- JUnit 5 and Mockito tests for core banking behavior.

## Demo Accounts

Seed data is created automatically when `SEED_DATA=true`.

| Role | Email | Password |
| --- | --- | --- |
| Customer | `demo.customer@banksphere.test` | `Customer@12345` |
| Customer | `demo.saver@banksphere.test` | `Customer@12345` |
| Admin | `admin@banksphere.test` | `Admin@12345` |

## Windows VS Code Terminal Commands

Prerequisites: Java 21, Maven 3.9+, Node.js 20+, npm, and either Docker Desktop or a local MySQL 8 server. On this machine Java and Node are present, but `mvn` and `docker` were not found on PATH.

Open VS Code in this folder, then run these commands in separate terminals.

1. Start MySQL:

```powershell
copy .env.example .env
docker compose up -d mysql
```

If you use a local MySQL server instead of Docker, create the database with `database/schema.sql`, then set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in your terminal or `.env`.

2. Run backend tests:

```powershell
cd backend
mvn test
```

3. Start the Spring Boot API:

```powershell
cd backend
mvn spring-boot:run
```

4. Install frontend dependencies:

```powershell
cd frontend
npm install
```

5. Start the React app:

```powershell
cd frontend
npm run dev
```

6. Open the website:

```text
http://localhost:5173
```

## Verify Features

- Register a new customer and confirm a demo savings account appears under My Accounts.
- Login as `demo.customer@banksphere.test`, then view dashboard totals from `/api/customer/dashboard`.
- Deposit and withdraw demo money, then confirm the transaction appears in Transaction History.
- Transfer to `781000000002`, then confirm balances and two transfer transactions are created.
- Export Account Statement as CSV and PDF.
- Login as `admin@banksphere.test`, suspend or activate a demo account, and check Audit Logs.
- Visit Swagger at `http://localhost:8080/swagger-ui.html` and authorize with a bearer JWT from login.

## Configuration

Backend environment variables:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JPA_DDL_AUTO`
- `JWT_SECRET`
- `JWT_EXPIRATION_MS`
- `CORS_ALLOWED_ORIGINS`
- `SEED_DATA`

Frontend environment variable:

- `VITE_API_BASE_URL`

## API Overview

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `GET /api/customer/dashboard`
- `GET /api/accounts`
- `POST /api/transactions/deposit`
- `POST /api/transactions/withdraw`
- `POST /api/transfers`
- `GET /api/transactions`
- `GET /api/statements.csv`
- `GET /api/statements.pdf`
- `GET /api/admin/stats`
- `GET /api/admin/customers`
- `GET /api/admin/accounts`
- `PATCH /api/admin/accounts/{accountId}/status`
- `GET /api/admin/transactions`
- `GET /api/admin/audit-logs`
