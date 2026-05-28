

## 1) Partner API endpoints (code snippet)

File: `src/main/java/com/lodging/Restarurant/controller/BookingApiController.java`

Key endpoints:
- `GET /api/v1/rooms`
- `POST /api/v1/bookings`
- `PUT /api/v1/bookings/{id}`
- `PUT /api/v1/bookings/{id}/cancel`
- `GET /api/v1/bookings`
- `GET /api/v1/bookings/{id}`

---

## 2) API key enforcement + usage logs (code snippet)

File: `src/main/java/com/lodging/Restarurant/config/ApiKeyFilter.java`

Highlights:
- checks `X-API-KEY`
- sets request attributes (partner name + customer id)
- logs endpoint/method/status to DB after request finishes

---

## 3) Analytics summary computation (code snippet)

File: `src/main/java/com/lodging/Restarurant/service/BookingService.java`

Highlights:
- `getAnalyticsSummary()` computes counts + revenue
- revenue counts CONFIRMED / CHECKED_IN / CHECKED_OUT only

---

## 4) LLM assistant endpoint (code snippet)

File: `src/main/java/com/lodging/Restarurant/controller/GroqController.java`

Highlights:
- model: `llama-3.1-8b-instant`
- prompt includes available room context

---

## 5) Razorpay payment integration (code snippet + flow)

Files:
- `src/main/java/com/lodging/Restarurant/service/PaymentService.java`
- `src/main/java/com/lodging/Restarurant/controller/PaymentController.java`
- UI: `src/main/resources/templates/customer/my-bookings.html` (Pay Now button)

Flow:
1. Customer books → booking **PENDING**
2. Staff confirms → booking **CONFIRMED**
3. Customer clicks **Pay Now**
4. Backend creates Razorpay order
5. Frontend opens Razorpay Checkout
6. Backend verifies signature and marks `Payment` as **PAID**

---

## 6) Outputs (copy/paste screenshots)

### A) Start app output
Run:

```powershell
.\gradlew bootRun
```

Take a screenshot showing:
- server started on port 8010
- seeder message confirming demo logins

### B) Partner API output

PowerShell (replace key):

```powershell
$base = "http://localhost:8010"
$headers = @{ "X-API-KEY" = "YOUR_KEY"; "Content-Type" = "application/json" }

Invoke-RestMethod "$base/api/v1/rooms" -Headers $headers
```

Screenshot the JSON output.

### C) Dashboard screenshots
Capture:
- `/admin/dashboard`
- `/admin/analytics`
- `/admin/api-dashboard`

### D) LLM screenshots
Capture:
- `/customer/ai-suggest` conversation

### E) Razorpay screenshots
Capture:
- Pay Now button visible on `/customer/bookings` for confirmed booking
- Razorpay checkout popup
- “Paid” badge after successful verification

