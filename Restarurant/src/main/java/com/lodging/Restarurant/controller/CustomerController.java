package com.lodging.Restarurant.controller;

import com.lodging.Restarurant.model.Booking;
import com.lodging.Restarurant.model.Room;
import com.lodging.Restarurant.model.User;
import com.lodging.Restarurant.model.enums.BookingStatus;
import com.lodging.Restarurant.service.BookingService;
import com.lodging.Restarurant.service.PaymentService;
import com.lodging.Restarurant.service.RoomService;
import com.lodging.Restarurant.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final BookingService bookingService;
    private final RoomService roomService;
    private final UserService userService;
    private final PaymentService paymentService;

    private User getLoggedInUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername());
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails,
                            Model model) {
        User user = getLoggedInUser(userDetails);
        List<Booking> bookings = bookingService.findByCustomer(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("bookings", bookings);
        model.addAttribute("totalBookings", bookings.size());
        model.addAttribute("activeBookings",
                bookings.stream()
                        .filter(b -> b.getStatus() == BookingStatus.CONFIRMED
                                || b.getStatus() == BookingStatus.CHECKED_IN)
                        .count());
        model.addAttribute("availableRooms", roomService.countAvailable());
        return "customer/dashboard";
    }

    @GetMapping("/book/{roomId}")
    public String bookRoomPage(@PathVariable Long roomId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model,
                               RedirectAttributes flash) {
        Room room = roomService.findById(roomId);
        if (!room.isAvailable()) {
            flash.addFlashAttribute("errorMsg", "This room is not available for booking.");
            return "redirect:/rooms";
        }
        model.addAttribute("room", room);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("tomorrow", LocalDate.now().plusDays(1));
        return "customer/book-room";
    }

    @PostMapping("/book")
    public String confirmBooking(@RequestParam Long roomId,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                 LocalDate checkIn,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                 LocalDate checkOut,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes flash) {
        try {
            User user = getLoggedInUser(userDetails);
            bookingService.createBooking(user.getId(), roomId, checkIn, checkOut);
            flash.addFlashAttribute("successMsg",
                    "Booking submitted! Our staff will confirm it shortly.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/customer/book/" + roomId;
        }
        return "redirect:/customer/bookings";
    }

    @GetMapping("/bookings")
    public String myBookings(@AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        User user = getLoggedInUser(userDetails);
        model.addAttribute("bookings", bookingService.findByCustomer(user.getId()));
        model.addAttribute("razorpayEnabled", paymentService.isConfigured());
        return "customer/my-bookings";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes flash) {
        try {
            User user = getLoggedInUser(userDetails);
            bookingService.cancelBooking(id, user.getId());
            flash.addFlashAttribute("successMsg", "Booking cancelled successfully.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/customer/bookings";
    }

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        model.addAttribute("user", getLoggedInUser(userDetails));
        return "customer/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String fullName,
                                @RequestParam String phone,
                                RedirectAttributes flash) {
        User user = getLoggedInUser(userDetails);
        userService.updateProfile(user.getId(), fullName, phone);
        flash.addFlashAttribute("successMsg", "Profile updated successfully.");
        return "redirect:/customer/profile";
    }
}
