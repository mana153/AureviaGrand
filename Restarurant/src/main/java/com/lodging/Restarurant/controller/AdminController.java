package com.lodging.Restarurant.controller;

import com.lodging.Restarurant.model.ApiKey;
import com.lodging.Restarurant.model.Booking;
import com.lodging.Restarurant.model.Room;
import com.lodging.Restarurant.model.User;
import com.lodging.Restarurant.model.enums.BookingStatus;
import com.lodging.Restarurant.model.enums.RoomType;
import com.lodging.Restarurant.repository.ApiKeyRepository;
import com.lodging.Restarurant.repository.ApiUsageLogRepository;
import com.lodging.Restarurant.service.BookingService;
import com.lodging.Restarurant.service.RoomService;
import com.lodging.Restarurant.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final BookingService bookingService;
    private final RoomService roomService;
    private final UserService userService;
    private final ApiKeyRepository apiKeyRepository;
    private final ApiUsageLogRepository apiUsageLogRepository;
    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Room> rooms = roomService.findAll();
        long totalRooms     = rooms.size();
        long availableRooms = rooms.stream().filter(Room::isAvailable).count();

        List<Booking> allBookings = bookingService.findAll();
        long totalBookings = allBookings.size();
        long pendingCount  = allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING).count();

        BigDecimal totalRevenue = allBookings.stream()
                .filter(b -> BookingService.countsTowardRevenue(b.getStatus()))
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalRooms",     totalRooms);
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("totalBookings",  totalBookings);
        model.addAttribute("pendingCount",   pendingCount);
        model.addAttribute("totalRevenue",   totalRevenue);
        model.addAttribute("recentBookings", allBookings);
        model.addAttribute("totalUsers",     userService.findAll().size());
        return "admin/dashboard";
    }

    // ── Room Management ───────────────────────────────────────────────────────

    @GetMapping("/rooms")
    public String rooms(Model model) {
        model.addAttribute("rooms", roomService.findAll());
        model.addAttribute("roomTypes", RoomType.values());
        model.addAttribute("newRoom", new Room());
        return "admin/rooms";
    }

    @PostMapping("/rooms")
    public String createRoom(@ModelAttribute Room room, RedirectAttributes flash) {
        try {
            room.setAvailable(true);
            roomService.save(room);
            flash.addFlashAttribute("successMsg", "Room created successfully.");
        } catch (Exception e) {
            flash.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/admin/rooms";
    }

    @GetMapping("/rooms/{id}/edit")
    public String editRoomForm(@PathVariable Long id, Model model) {
        Room room = roomService.findById(id);
        model.addAttribute("room", room);
        model.addAttribute("roomTypes", RoomType.values());
        return "admin/room-edit";
    }

    @PostMapping("/rooms/{id}/edit")
    public String updateRoom(@PathVariable Long id,
                             @ModelAttribute Room updated,
                             RedirectAttributes flash) {
        try {
            Room existing = roomService.findById(id);
            existing.setRoomNumber(updated.getRoomNumber());
            existing.setType(updated.getType());
            existing.setPricePerNight(updated.getPricePerNight());
            existing.setCapacity(updated.getCapacity());
            existing.setDescription(updated.getDescription());
            existing.setImageUrl(updated.getImageUrl());
            existing.setAvailable(updated.isAvailable());
            roomService.save(existing);
            flash.addFlashAttribute("successMsg", "Room updated.");
        } catch (Exception e) {
            flash.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/rooms";
    }

    @PostMapping("/rooms/{id}/delete")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes flash) {
        try {
            roomService.delete(id);
            flash.addFlashAttribute("successMsg", "Room deleted.");
        } catch (Exception e) {
            flash.addFlashAttribute("errorMsg", "Cannot delete: " + e.getMessage());
        }
        return "redirect:/admin/rooms";
    }

    // ── User Management ───────────────────────────────────────────────────────

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, RedirectAttributes flash) {
        try {
            userService.toggleUserStatus(id);
            flash.addFlashAttribute("successMsg", "User status updated.");
        } catch (Exception e) {
            flash.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/users";
    }


    // ── Analytics ─────────────────────────────────────────────────────────────

    @GetMapping("/analytics")
    public String analytics(Model model) {
        Map<String, Object> summary = bookingService.getAnalyticsSummary();
        model.addAllAttributes(summary);
        return "admin/analytics";
    }
    // ── API Key Management ────────────────────────────────────────────────────

    @GetMapping("/api-dashboard")
    public String apiDashboard(Model model) {
        model.addAttribute("apiKeys", apiKeyRepository.findAll());
        model.addAttribute("logs",    apiUsageLogRepository.findTop100ByOrderByCalledAtDesc());

        // Endpoint usage counts
        List<Object[]> endpointStats = apiUsageLogRepository.countByEndpoint();
        List<Object[]> partnerStats  = apiUsageLogRepository.countByPartner();
        model.addAttribute("endpointStats", endpointStats);
        model.addAttribute("partnerStats",  partnerStats);
        model.addAttribute("totalCalls",    apiUsageLogRepository.count());

        model.addAttribute("newKey", new ApiKey());
        return "admin/api-dashboard";
    }

    @GetMapping("/api")
    public String apiDashboardAlias() {
        return "redirect:/admin/api-dashboard";
    }

    @PostMapping("/api-keys/create")
    public String createKey(@RequestParam String partnerName,
                            @RequestParam Long customerId,
                            RedirectAttributes flash) {
        try {
            userService.requireCustomer(customerId);
        } catch (RuntimeException e) {
            flash.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/admin/api-dashboard";
        }
        String keyValue = "lh_" + partnerName.toLowerCase().replaceAll("\\s+","_")
                + "_" + java.util.UUID.randomUUID().toString().substring(0, 8);
        apiKeyRepository.save(ApiKey.builder()
                .keyValue(keyValue)
                .partnerName(partnerName)
                .customerId(customerId)
                .active(true)
                .createdAt(java.time.LocalDateTime.now())
                .build());
        flash.addFlashAttribute("successMsg", "API key created: " + keyValue);
        return "redirect:/admin/api-dashboard";
    }

    @PostMapping("/api-keys/{id}/toggle")
    public String toggleKey(@PathVariable Long id, RedirectAttributes flash) {
        apiKeyRepository.findById(id).ifPresent(k -> {
            k.setActive(!k.isActive());
            apiKeyRepository.save(k);
        });
        flash.addFlashAttribute("successMsg", "Key status updated.");
        return "redirect:/admin/api-dashboard";
    }
}