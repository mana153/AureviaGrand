package com.lodging.Restarurant.controller;

import com.lodging.Restarurant.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class PublicController {

    private final RoomService roomService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("featuredRooms", roomService.findFeatured());
        return "public/index";
    }

    @GetMapping("/about")
    public String about() {
        return "public/about";
    }

    @GetMapping("/rooms")
    public String rooms(Model model,
                        @RequestParam(required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
                        @RequestParam(required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        try {
            model.addAttribute("rooms", roomService.findBookable(checkIn, checkOut));
        } catch (RuntimeException e) {
            model.addAttribute("rooms", roomService.findBookable(null, null));
            model.addAttribute("errorMsg", e.getMessage());
        }
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        model.addAttribute("today", LocalDate.now());
        return "public/rooms";
    }
}