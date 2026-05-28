# LLM Feature (Groq) — AI Room Assistant (Submission Doc)

Feature: **LLM-based room recommendation assistant**  
Provider: **Groq** (`https://groq.com/`)  
Audience: Customers (`ROLE_CUSTOMER`)

---

## 1) What was built

The application provides an AI chat-like interface for customers to get room recommendations.

UI page:
- `GET /customer/ai-suggest`
- Template: `src/main/resources/templates/customer/ai-suggest.html`

Backend endpoint:
- `POST /api/ai/suggest`
- Controller: `src/main/java/com/lodging/Restarurant/controller/GroqController.java`

The assistant:
- builds a short “room context” from currently available rooms
- asks the LLM to suggest the best match based on the user message
- returns JSON `{ "reply": "..." }`

---

## 2) Configuration

Environment variable:
- `GROQ_API_KEY`

Property:
- `groq.api.url=https://api.groq.com/openai/v1/chat/completions`

If the key is missing, the UI shows a warning and the API replies with a helpful configuration message.

---

## 3) How it works (implementation details)

### Room context
The controller loads rooms via `RoomService.findAll()` and formats available rooms into a system prompt.

### Groq model
The controller uses a Groq-hosted LLM model:
- model: `llama-3.1-8b-instant`

### Request format
`GroqController` sends:
- `model`
- `messages` list containing `system` + `user`
- `max_tokens`
- `temperature`

### Error handling
The controller:
- returns a human-friendly response if Groq is unavailable
- logs HTTP errors for troubleshooting

---

## 4) What to screenshot (for your PDF submission)

1. Customer login → redirected to `/customer/dashboard`
2. Customer opens `/customer/ai-suggest`
3. Chat interaction:
   - user message
   - AI reply
4. Optional: screenshot of `application.properties` showing `groq.api.url` and use of `GROQ_API_KEY` env var

---

## 5) Files involved (implementation references)

- `src/main/java/com/lodging/Restarurant/controller/GroqController.java`
- `src/main/resources/templates/customer/ai-suggest.html`
- `src/main/resources/application.properties` (Groq URL + env var key)

