package com.lodging.Restarurant.repository;

import com.lodging.Restarurant.model.Room;
import com.lodging.Restarurant.model.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByIsAvailableTrue();

    List<Room> findByType(RoomType type);

    // Rooms not booked in the given date range
    @Query("""
        SELECT r FROM Room r WHERE r.isAvailable = true
        AND r.id NOT IN (
            SELECT b.room.id FROM Booking b
            WHERE b.status NOT IN ('CANCELLED', 'CHECKED_OUT')
            AND NOT (b.checkOutDate <= :checkIn OR b.checkInDate >= :checkOut)
        )
    """)
    List<Room> findAvailableRooms(@Param("checkIn") LocalDate checkIn,
                                  @Param("checkOut") LocalDate checkOut);
}