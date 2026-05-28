# Dashboard & Analytics Feature (Submission Doc)

Feature: **Analytics dashboard showing stored application data**  
Audience: Admin users (`ROLE_ADMIN`)

---

## 1) What was built

The application includes an **Admin Analytics** page:
- URL: `GET /admin/analytics`
- Template: `src/main/resources/templates/admin/analytics.html`

It visualizes:
- booking lifecycle counts (PENDING / CONFIRMED / CHECKED_IN / CHECKED_OUT / CANCELLED)
- total revenue (counts **CONFIRMED / CHECKED_IN / CHECKED_OUT** only)
- revenue breakdown by room type
- charts rendered in the browser via Chart.js

Additionally, the Admin Dashboard page `GET /admin/dashboard` shows:
- total rooms / available rooms
- total bookings
- total users
- pending approvals
- total revenue (confirmed+)
- table of all bookings including booking source (Web vs Partner)

---

## 2) Data sources (how it works)

### Booking + Room data
- Bookings and rooms are stored in PostgreSQL (`hotel_db`) via Spring Data JPA.
- The analytics data is computed at runtime from persisted bookings.

### Computation
`BookingService.getAnalyticsSummary()` returns a `Map<String, Object>` with:
- status counts
- totalRevenue
- revenueByType + chart labels/values

Controller:
- `AdminController.analytics()` calls `bookingService.getAnalyticsSummary()` and adds the map to the model.

---

## 3) What to screenshot (for your PDF submission)

Take screenshots of:
1. **Admin login** success → redirected to `/admin/dashboard`
2. `/admin/dashboard` showing:
   - KPI cards
   - booking table (and “Source”: Web vs partner)
3. `/admin/analytics` showing:
   - KPI cards
   - charts
   - revenue table
4. Optional: `/admin/api-dashboard` showing usage charts (counts by endpoint/partner)

---

## 4) How to reproduce the data quickly

1. Login as Customer and create at least 1 booking (status becomes PENDING).
2. Login as Staff and confirm the booking (status becomes CONFIRMED).
3. (Optional) Staff: check-in, then check-out.
4. Login as Admin and open `/admin/analytics` and `/admin/dashboard` to see the metrics change.

---

## 5) Files involved (implementation references)

- `src/main/java/com/lodging/Restarurant/controller/AdminController.java`
- `src/main/java/com/lodging/Restarurant/service/BookingService.java` (`getAnalyticsSummary`, revenue rules)
- `src/main/resources/templates/admin/analytics.html`
- `src/main/resources/templates/admin/dashboard.html`

