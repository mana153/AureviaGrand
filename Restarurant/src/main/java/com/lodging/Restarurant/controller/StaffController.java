package com.lodging.Restarurant.controller;

import com.lodging.Restarurant.model.Booking;
import com.lodging.Restarurant.model.RoomOrder;
import com.lodging.Restarurant.model.User;
import com.lodging.Restarurant.model.enums.BookingStatus;
import com.lodging.Restarurant.model.enums.RoomOrderStatus;
import com.lodging.Restarurant.service.BookingService;
import com.lodging.Restarurant.service.RoomOrderService;
import com.lodging.Restarurant.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final BookingService bookingService;
    private final UserService userService;
    private final RoomOrderService roomOrderService;

    private User getLoggedInUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername());
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails,
                            Model model) {
        User staff = getLoggedInUser(userDetails);
        List<Booking> bookings = bookingService.findAll();
        model.addAttribute("staff", staff);
        model.addAttribute("bookings", bookings);
        model.addAttribute("pendingCount",
                bookings.stream().filter(b -> b.getStatus() == BookingStatus.PENDING).count());
        model.addAttribute("confirmedCount",
                bookings.stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count());
        model.addAttribute("checkedInCount",
                bookings.stream().filter(b -> b.getStatus() == BookingStatus.CHECKED_IN).count());
        List<RoomOrder> roomOrders = roomOrderService.findAll();
        model.addAttribute("roomOrders", roomOrders);
        model.addAttribute("orderPendingCount",
                roomOrders.stream().filter(o -> o.getStatus() == RoomOrderStatus.PENDING).count());
        model.addAttribute("orderPreparingCount",
                roomOrders.stream().filter(o -> o.getStatus() == RoomOrderStatus.PREPARING).count());
        model.addAttribute("orderDeliveredCount",
                roomOrders.stream().filter(o -> o.getStatus() == RoomOrderStatus.DELIVERED).count());
        Map<Long, java.math.BigDecimal> runningBills = roomOrders.stream()
                .filter(o -> o.getBooking() != null)
                .collect(Collectors.toMap(
                        o -> o.getBooking().getId(),
                        o -> o.getBooking().getTotalPrice().add(
                                roomOrderService.totalFoodChargesForBooking(o.getBooking().getId())),
                        (a, b) -> a
                ));
        model.addAttribute("runningBills", runningBills);
        return "staff/dashboard";
    }

    @GetMapping("/bookings")
    public String bookings(@RequestParam(required = false) String status, Model model) {
        List<Booking> bookings = bookingService.findAll();
        if (status != null && !status.isBlank()) {
            try {
                BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
                bookings = bookings.stream()
                        .filter(b -> b.getStatus() == bookingStatus)
                        .toList();
            } catch (IllegalArgumentException ignored) {
                // invalid filter value -> keep full list
            }
        }
        model.addAttribute("bookings", bookings);
        model.addAttribute("activeStatusFilter", status);
        return "staff/bookings";
    }

    @PostMapping("/bookings/{id}/confirm")
    public String confirm(@PathVariable Long id,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes flash) {
        try {
            User staff = getLoggedInUser(userDetails);
            bookingService.confirmBooking(id, staff.getId());
            flash.addFlashAttribute("successMsg", "Booking #" + id + " confirmed.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/bookings";
    }

    @PostMapping("/bookings/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes flash) {
        try {
            bookingService.rejectBooking(id);
            flash.addFlashAttribute("successMsg", "Booking #" + id + " rejected.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/bookings";
    }

    @GetMapping("/checkin")
    public String checkinPage(Model model) {
        model.addAttribute("bookings", bookingService.findAll());
        return "staff/checkin-checkout";
    }

    @PostMapping("/checkin/{id}/checkin")
    public String doCheckIn(@PathVariable Long id, RedirectAttributes flash) {
        try {
            bookingService.checkIn(id);
            flash.addFlashAttribute("successMsg", "Guest checked in for booking #" + id);
        } catch (RuntimeException e) {
            flash.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/checkin";
    }

    @PostMapping("/checkin/{id}/checkout")
    public String doCheckOut(@PathVariable Long id, RedirectAttributes flash) {
        try {
            bookingService.checkOut(id);
            flash.addFlashAttribute("successMsg", "Guest checked out for booking #" + id);
        } catch (RuntimeException e) {
            flash.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/checkin";
    }

    @PostMapping("/room-orders/{id}/status")
    public String updateRoomOrderStatus(@PathVariable Long id,
                                        @RequestParam RoomOrderStatus status,
                                        RedirectAttributes flash) {
        try {
            roomOrderService.updateStatus(id, status);
            flash.addFlashAttribute("successMsg", "Room service order #" + id + " updated to " + status + ".");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/dashboard";
    }

    @PostMapping("/room-orders/walk-in")
    public String addWalkInOrder(@RequestParam String walkInName,
                                 @RequestParam String venueSlug,
                                 @RequestParam String venueName,
                                 @RequestParam String itemName,
                                 @RequestParam String itemDescription,
                                 @RequestParam String itemPrice,
                                 @RequestParam int quantity,
                                 RedirectAttributes flash) {
        try {
            roomOrderService.createWalkInOrder(
                    walkInName,
                    venueSlug,
                    venueName,
                    itemName,
                    itemDescription,
                    new java.math.BigDecimal(itemPrice),
                    quantity
            );
            flash.addFlashAttribute("successMsg", "Walk-in order created successfully.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/dashboard";
    }
}
