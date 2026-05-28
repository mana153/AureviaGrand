# API Documentation (Partner Integrations)

Project: **AureviaGrand — Lodging & Restaurant Management System**  
Base URL (local): `http://localhost:8010`  

This document covers the **third‑party partner APIs** for companies like **Zomato / Swiggy** to integrate lodging services.

---

## 1. Authentication (API Key)

All partner endpoints are under:

- **Prefix**: `/api/v1/**`
- **Auth**: `X-API-KEY` header

### Required headers

```
X-API-KEY: <partner_api_key>
Content-Type: application/json
```

### How to get an API key (Admin UI)

1. Login as Admin (see credentials in `docs/Runbook_and_Credentials.md`)
2. Go to `GET /admin/api-dashboard`
3. Generate a new key using:
   - Partner name (e.g. `Zomato`)
   - Customer user ID (a valid `ROLE_CUSTOMER` user ID from Admin → Users)

The dashboard also shows:
- active keys
- last used timestamp
- endpoint usage stats
- last 100 call logs

---

## 2. Endpoints Summary (Listing / Creating / Updating / Cancelling)

| Requirement | Method | Endpoint |
|------------|--------|----------|
| Listing rooms | GET | `/api/v1/rooms` |
| Listing bookings | GET | `/api/v1/bookings` |
| Creating booking | POST | `/api/v1/bookings` |
| Updating booking | PUT | `/api/v1/bookings/{id}` |
| Cancelling booking | PUT | `/api/v1/bookings/{id}/cancel` |

> **Partner data isolation**: A partner can only view/update/cancel bookings created by that partner key (tracked as `sourcePartner` on bookings).

---

## 3. Listing Rooms

### `GET /api/v1/rooms`

Lists bookable rooms.

#### Query params (optional)
- `checkIn` (ISO date, `YYYY-MM-DD`)
- `checkOut` (ISO date, `YYYY-MM-DD`)

Rule: **either provide both or neither**.

#### Response 200 (example)

```json
[
  {
    "id": 1,
    "roomNumber": "101",
    "type": "SINGLE",
    "pricePerNight": 6500.00,
    "capacity": 1,
    "description": "Luxury single room with skyline views.",
    "imageUrl": "https://images.unsplash.com/..."
  }
]
```

---

## 4. Creating a Booking

### `POST /api/v1/bookings`

Creates a new booking (status: **PENDING**) for the customer bound to the API key.

#### Body

```json
{
  "roomId": 1,
  "checkIn": "2026-06-01",
  "checkOut": "2026-06-03"
}
```

`customerId` is optional if the key is tied to a customer; if sent, it must match the key’s customer.

#### Response 200 (example)

```json
{
  "bookingId": 10,
  "status": "PENDING",
  "totalPrice": 13000.00,
  "message": "Booking created successfully"
}
```

#### Errors
- 401: invalid/missing API key
- 400: bad input dates / room not available / customer mismatch

---

## 5. Listing Bookings (Partner Scope)

### `GET /api/v1/bookings`

Returns bookings created by this partner (only).

#### Response 200 (example)

```json
[
  {
    "id": 10,
    "status": "PENDING",
    "roomNumber": "101",
    "checkIn": "2026-06-01",
    "checkOut": "2026-06-03",
    "totalPrice": 13000.00
  }
]
```

---

## 6. Get Booking Detail

### `GET /api/v1/bookings/{id}`

Returns details for a booking created by this partner.

#### Response 200 (example)

```json
{
  "id": 10,
  "status": "PENDING",
  "roomNumber": "101",
  "checkIn": "2026-06-01",
  "checkOut": "2026-06-03",
  "totalPrice": 13000.00,
  "guest": "Mana Dhanak"
}
```

#### Response 404
Returned if booking does not exist **or** belongs to a different partner (to avoid leaking existence).

---

## 7. Updating a Booking (Partner)

### `PUT /api/v1/bookings/{id}`

Updates the booking **only if**:
- booking belongs to this partner
- booking status is **PENDING**
- new dates do not overlap with existing bookings

#### Body (example)

```json
{
  "roomId": 2,
  "checkIn": "2026-06-05",
  "checkOut": "2026-06-07"
}
```

#### Response 200 (example)

```json
{
  "bookingId": 10,
  "status": "PENDING",
  "totalPrice": 23000.00,
  "message": "Booking updated successfully"
}
```

---

## 8. Cancelling a Booking (Partner)

### `PUT /api/v1/bookings/{id}/cancel`

Cancels a booking only if:
- booking belongs to this partner
- booking is cancellable (**PENDING** or **CONFIRMED**)

#### Body

```json
{}
```

#### Response 200 (example)

```json
{
  "bookingId": 10,
  "status": "CANCELLED",
  "message": "Booking cancelled successfully"
}
```

---

## 9. API Dashboard (Admin)

UI page:
- `GET /admin/api-dashboard`

Provides:
- create/revoke keys
- usage logs (latest 100)
- charts: calls by endpoint / calls by partner

---

## 10. PowerShell examples

```powershell
$base = "http://localhost:8010"
$headers = @{ "X-API-KEY" = "YOUR_KEY"; "Content-Type" = "application/json" }

# 1) Listing rooms
Invoke-RestMethod "$base/api/v1/rooms" -Headers $headers

# 2) Create booking
$body = @{ roomId = 1; checkIn = "2026-06-01"; checkOut = "2026-06-03" } | ConvertTo-Json
Invoke-RestMethod "$base/api/v1/bookings" -Method Post -Headers $headers -Body $body

# 3) Update booking
$update = @{ roomId = 2; checkIn = "2026-06-05"; checkOut = "2026-06-07" } | ConvertTo-Json
Invoke-RestMethod "$base/api/v1/bookings/10" -Method Put -Headers $headers -Body $update

# 4) Cancel booking
Invoke-RestMethod "$base/api/v1/bookings/10/cancel" -Method Put -Headers $headers -Body "{}"
```

