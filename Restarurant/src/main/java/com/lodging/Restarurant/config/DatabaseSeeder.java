package com.lodging.Restarurant.config;

import com.lodging.Restarurant.model.Role;
import com.lodging.Restarurant.model.Room;
import com.lodging.Restarurant.model.User;
import com.lodging.Restarurant.model.enums.RoomType;
import com.lodging.Restarurant.repository.RoleRepository;
import com.lodging.Restarurant.repository.RoomRepository;
import com.lodging.Restarurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements ApplicationRunner {

    private static final Map<String, String> DEMO_USERS = new LinkedHashMap<>();

    static {
        DEMO_USERS.put("admin@hotel.com", "admin123");
        DEMO_USERS.put("staff@hotel.com", "staff123");
        DEMO_USERS.put("mana@email.com", "mana123");
        DEMO_USERS.put("fiona@email.com", "fiona123");
    }

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.demo.sync-passwords:true}")
    private boolean syncDemoPasswords;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedRoles();
        seedDemoUsers();
        seedRoomsIfEmpty();
        log.info("Database seed check complete. Demo logins: admin@hotel.com / admin123, staff@hotel.com / staff123");
    }

    private void seedRoles() {
        for (String name : new String[]{"ROLE_ADMIN", "ROLE_STAFF", "ROLE_CUSTOMER"}) {
            roleRepository.findByName(name).orElseGet(() ->
                    roleRepository.save(new Role(name)));
        }
    }

    private void seedDemoUsers() {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
        Role staffRole = roleRepository.findByName("ROLE_STAFF").orElseThrow();
        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER").orElseThrow();

        Map<String, Role> roleByEmail = Map.of(
                "admin@hotel.com", adminRole,
                "staff@hotel.com", staffRole,
                "mana@email.com", customerRole,
                "fiona@email.com", customerRole
        );

        Map<String, String> names = Map.of(
                "admin@hotel.com", "Hotel Admin",
                "staff@hotel.com", "Reception Staff",
                "mana@email.com", "Mana Dhanak",
                "fiona@email.com", "Fiona Williams"
        );

        DEMO_USERS.forEach((email, password) -> {
            Role role = roleByEmail.get(email);
            User user = userRepository.findByEmail(email).orElseGet(() ->
                    User.builder()
                            .email(email)
                            .fullName(names.get(email))
                            .phone("9000000000")
                            .role(role)
                            .active(true)
                            .build());

            if (syncDemoPasswords || user.getId() == null) {
                user.setPassword(passwordEncoder.encode(password));
            }
            user.setRole(role);
            user.setActive(true);
            userRepository.save(user);
        });
    }

    private void seedRoomsIfEmpty() {
        if (roomRepository.count() > 0) return;

        roomRepository.save(Room.builder()
                .roomNumber("101").type(RoomType.SINGLE)
                .pricePerNight(new BigDecimal("6500")).capacity(1)
                .description("Luxury single room with skyline views.")
                .imageUrl("https://images.unsplash.com/photo-1566073771259-6a8506099945")
                .available(true).build());
        roomRepository.save(Room.builder()
                .roomNumber("201").type(RoomType.DOUBLE)
                .pricePerNight(new BigDecimal("11500")).capacity(2)
                .description("Elegant double room with lounge seating.")
                .imageUrl("https://images.unsplash.com/photo-1590490360182-c33d57733427")
                .available(true).build());
        roomRepository.save(Room.builder()
                .roomNumber("301").type(RoomType.DELUXE)
                .pricePerNight(new BigDecimal("18500")).capacity(3)
                .description("Deluxe suite with panoramic views.")
                .imageUrl("https://images.unsplash.com/photo-1542314831-068cd1dbfeeb")
                .available(true).build());
        roomRepository.save(Room.builder()
                .roomNumber("401").type(RoomType.SUITE)
                .pricePerNight(new BigDecimal("32000")).capacity(4)
                .description("Presidential suite with butler service.")
                .imageUrl("https://images.unsplash.com/photo-1578898887932-dce23a595ad4")
                .available(true).build());
        log.info("Seeded sample rooms.");
    }
}
