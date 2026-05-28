package com.lodging.Restarurant.controller;

import com.lodging.Restarurant.model.Room;
import com.lodging.Restarurant.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GroqController {

    private static final String GROQ_MODEL = "llama-3.1-8b-instant";

    private final RoomService roomService;
    private final RestTemplate restTemplate;

    @Value("${groq.api.key:}")
    private String groqApiKey;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String groqApiUrl;

    @GetMapping("/customer/ai-suggest")
    public String aiPage(Model model) {
        model.addAttribute("rooms", roomService.findAll());
        model.addAttribute("aiConfigured", groqApiKey != null && !groqApiKey.isBlank());
        return "customer/ai-suggest";
    }

    @PostMapping("/api/ai/suggest")
    @ResponseBody
    public ResponseEntity<Map<String, String>> suggest(@RequestBody Map<String, String> body) {
        if (groqApiKey == null || groqApiKey.isBlank()) {
            return ResponseEntity.ok(Map.of("reply",
                    "AI assistant is not configured. Ask the hotel to set GROQ_API_KEY."));
        }

        String userMessage = body.getOrDefault("message", "").trim();
        if (userMessage.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message is required."));
        }

        List<Room> rooms = roomService.findAll();
        String roomContext = rooms.stream()
                .filter(Room::isAvailable)
                .map(r -> String.format("Room %s (%s) — ₹%.0f/night, capacity %d. %s",
                        r.getRoomNumber(), r.getType(),
                        r.getPricePerNight(), r.getCapacity(),
                        r.getDescription() != null ? r.getDescription() : ""))
                .collect(Collectors.joining("\n"));

        String systemPrompt = """
                You are a helpful luxury hotel booking assistant.
                Your job is to recommend rooms based on the guest's needs.
                Always be concise, warm, and professional.
                Here are the currently available rooms:
                
                """ + roomContext + """
                
                Suggest the best matching room and explain why.
                If the guest asks about pricing, check-in, or policies, answer helpfully.
                Keep responses under 100 words.
                """;

        Map<String, Object> requestBody = Map.of(
                "model", GROQ_MODEL,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user",   "content", userMessage)
                ),
                "max_tokens", 256,
                "temperature", 0.7
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    groqApiUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    Map.class
            );

            Map<?, ?> responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("choices")) {
                return ResponseEntity.ok(Map.of("reply",
                        "Sorry, I could not generate a response. Please try again."));
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices =
                    (List<Map<String, Object>>) responseBody.get("choices");
            @SuppressWarnings("unchecked")
            Map<String, String> message =
                    (Map<String, String>) choices.get(0).get("message");
            String reply = message.get("content");

            return ResponseEntity.ok(Map.of("reply", reply != null ? reply : "No response."));

        } catch (RestClientResponseException e) {
            log.warn("Groq API error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.ok(Map.of("reply",
                    "Sorry, the assistant is temporarily unavailable. Please try again shortly."));
        } catch (Exception e) {
            log.warn("Groq request failed", e);
            return ResponseEntity.ok(Map.of("reply",
                    "Sorry, I'm having trouble connecting right now. Please try again shortly."));
        }
    }
}
