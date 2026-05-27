package com.lodging.Restarurant.controller;

import com.lodging.Restarurant.model.Role;
import com.lodging.Restarurant.model.User;
import com.lodging.Restarurant.repository.RoleRepository;
import com.lodging.Restarurant.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Login ─────────────────────────────────────────────────────────────────

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) model.addAttribute("errorMsg", "Invalid email or password.");
        if (logout != null) model.addAttribute("logoutMsg", "You have been logged out.");
        return "auth/login";
    }

    // ── Register ──────────────────────────────────────────────────────────────

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
                           BindingResult result,
                           RedirectAttributes flash,
                           Model model) {

        // Duplicate email check
        if (userRepository.existsByEmail(user.getEmail())) {
            model.addAttribute("errorMsg", "Email already registered. Please login.");
            return "auth/register";
        }

        if (result.hasErrors()) {
            return "auth/register";
        }

        // Assign ROLE_CUSTOMER by default
        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new RuntimeException("ROLE_CUSTOMER not found in DB. Run seed SQL."));

        user.setRole(customerRole);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);
        userRepository.save(user);

        flash.addFlashAttribute("successMsg", "Account created! Please login.");
        return "redirect:/auth/login";
    }
}