package com.lodging.Restarurant.controller;

import com.lodging.Restarurant.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
    public String rooms(Model model) {
        model.addAttribute("rooms", roomService.findAll());
        return "public/rooms";
    }
}