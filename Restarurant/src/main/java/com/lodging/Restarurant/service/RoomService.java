package com.lodging.Restarurant.service;

import com.lodging.Restarurant.model.Room;
import com.lodging.Restarurant.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    // Show only first 3 available rooms on landing page
    public List<Room> findFeatured() {
        return roomRepository.findByIsAvailableTrue()
                .stream()
                .limit(3)
                .toList();
    }

    public Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found: " + id));
    }

    public List<Room> findAvailable(LocalDate checkIn, LocalDate checkOut) {
        return roomRepository.findAvailableRooms(checkIn, checkOut);
    }

    public Room save(Room room) {
        return roomRepository.save(room);
    }

    public void delete(Long id) {
        roomRepository.deleteById(id);
    }
}