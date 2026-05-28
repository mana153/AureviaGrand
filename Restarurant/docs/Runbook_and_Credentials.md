# Runbook + Login Credentials (Demo)

Project: **AureviaGrand — Lodging & Restaurant Management System**  
Local URL: `http://localhost:8010`

---

## 1) Start the application (Windows PowerShell)

From the project root (`Restarurant` folder):

```powershell
cd "C:\Users\Mana\Downloads\Restarurant\Restarurant"

# optional: run tests once
.\gradlew test

# run the server
.\gradlew bootRun
```

Then open:
- `http://localhost:8010`
- Login page: `http://localhost:8010/auth/login`

---

## 2) PostgreSQL connection (local)

The app is configured for PostgreSQL:
- DB: `hotel_db`
- URL: `jdbc:postgresql://localhost:5432/hotel_db`
- User: `postgres` (default unless you changed it)

### Create DB

```sql
CREATE DATABASE hotel_db;
```

### Config options

You can set these environment variables (recommended):
- `DB_USERNAME`
- `DB_PASSWORD`
- `GROQ_API_KEY`
- `RAZORPAY_KEY_ID`
- `RAZORPAY_KEY_SECRET`

Example PowerShell:

```powershell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"  # replace with your actual password
$env:GROQ_API_KEY="your_groq_key"
$env:RAZORPAY_KEY_ID="rzp_test_..."
$env:RAZORPAY_KEY_SECRET="..."
```

---

## 3) Demo logins (works after restart)

Demo users are ensured on startup by `DatabaseSeeder` (passwords are synced each start).

| Role | Email | Password | Redirect |
|------|-------|----------|----------|
| Admin | `admin@hotel.com` | `admin123` | `/admin/dashboard` |
| Staff | `staff@hotel.com` | `staff123` | `/staff/dashboard` |
| Customer | `mana@email.com` | `mana123` | `/customer/dashboard` |
| Customer | `fiona@email.com` | `fiona123` | `/customer/dashboard` |

---

## 4) Role-based functionality checklist

### Customer (Guest)
- Browse rooms: `GET /rooms` (supports date availability search)
- Book: `GET /customer/book/{roomId}` → submit dates → booking becomes **PENDING**
- View/cancel: `GET /customer/bookings` (cancel if **PENDING** / **CONFIRMED**)
- Profile: `GET/POST /customer/profile`
- AI assistant (Groq): `GET /customer/ai-suggest`, `POST /api/ai/suggest`

### Staff
- Dashboard: `GET /staff/dashboard`
- Confirm/reject PENDING: `GET /staff/bookings` + POST actions
- Check-in/out:
  - Check-in allowed only for **CONFIRMED**
  - Check-out allowed only for **CHECKED_IN**

### Admin
- Dashboard: `GET /admin/dashboard`
- Rooms CRUD: `GET/POST /admin/rooms`, edit/delete
- Users: `GET /admin/users` (activate/deactivate)
- Analytics dashboard: `GET /admin/analytics`
- Partner API dashboard: `GET /admin/api-dashboard` (keys + usage)

---

## 5) Razorpay payment flow (Customer)

Prerequisites:
- Staff must **confirm** a booking first (booking becomes **CONFIRMED**)
- Set `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET` and restart the app

Then Customer:
- Go to `GET /customer/bookings`
- For CONFIRMED booking, click **Pay Now**
- Razorpay checkout opens; on success booking shows **Paid**

---

## 6) Partner integration quick test

1. Login as Admin → `GET /admin/users` → note a **customer** ID.
2. Admin → `GET /admin/api-dashboard` → generate a key with partner name + customer ID.
3. Call partner endpoints using `X-API-KEY` header (see `docs/API_Documentation.md`).

