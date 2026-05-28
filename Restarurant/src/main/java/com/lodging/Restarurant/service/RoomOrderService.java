package com.lodging.Restarurant.service;

import com.lodging.Restarurant.model.Booking;
import com.lodging.Restarurant.model.RoomOrder;
import com.lodging.Restarurant.model.RoomOrderItem;
import com.lodging.Restarurant.model.User;
import com.lodging.Restarurant.model.enums.RoomOrderBillingType;
import com.lodging.Restarurant.model.enums.RoomOrderStatus;
import com.lodging.Restarurant.repository.RoomOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomOrderService {

    private final RoomOrderRepository roomOrderRepository;
    private final BookingService bookingService;

    @Transactional
    public RoomOrder placeCustomerOrder(User customer,
                                        String venueSlug,
                                        String venueName,
                                        String itemName,
                                        String itemDescription,
                                        BigDecimal unitPrice,
                                        int quantity,
                                        RoomOrderBillingType billingType) {
        if (quantity < 1) {
            throw new RuntimeException("Quantity must be at least 1.");
        }
        if (billingType == RoomOrderBillingType.WALK_IN_DIRECT) {
            throw new RuntimeException("Invalid billing type for guest room service order.");
        }

        Booking activeBooking = null;
        if (billingType == RoomOrderBillingType.CHARGE_TO_ROOM) {
            activeBooking = bookingService.findActiveCheckedInBooking(customer.getId());
        }

        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        RoomOrder order = RoomOrder.builder()
                .booking(activeBooking)
                .customer(customer)
                .venueSlug(venueSlug)
                .venueName(venueName)
                .billingType(billingType)
                .status(RoomOrderStatus.PENDING)
                .totalAmount(lineTotal)
                .build();

        RoomOrderItem item = RoomOrderItem.builder()
                .roomOrder(order)
                .itemName(itemName)
                .itemDescription(itemDescription)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .lineTotal(lineTotal)
                .build();
        order.getItems().add(item);
        return roomOrderRepository.save(order);
    }

    @Transactional
    public RoomOrder createWalkInOrder(String walkInName,
                                       String venueSlug,
                                       String venueName,
                                       String itemName,
                                       String itemDescription,
                                       BigDecimal unitPrice,
                                       int quantity) {
        if (walkInName == null || walkInName.isBlank()) {
            throw new RuntimeException("Walk-in guest name is required.");
        }
        if (quantity < 1) {
            throw new RuntimeException("Quantity must be at least 1.");
        }

        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        RoomOrder order = RoomOrder.builder()
                .walkInName(walkInName)
                .venueSlug(venueSlug)
                .venueName(venueName)
                .billingType(RoomOrderBillingType.WALK_IN_DIRECT)
                .status(RoomOrderStatus.PENDING)
                .totalAmount(lineTotal)
                .build();

        RoomOrderItem item = RoomOrderItem.builder()
                .roomOrder(order)
                .itemName(itemName)
                .itemDescription(itemDescription)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .lineTotal(lineTotal)
                .build();
        order.getItems().add(item);
        return roomOrderRepository.save(order);
    }

    public List<RoomOrder> findAll() {
        return roomOrderRepository.findAllByOrderByCreatedAtDesc();
    }

    public RoomOrder getById(Long id) {
        return roomOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room service order not found."));
    }

    @Transactional
    public void updateStatus(Long orderId, RoomOrderStatus status) {
        RoomOrder order = getById(orderId);
        order.setStatus(status);
        roomOrderRepository.save(order);
    }

    public BigDecimal totalFoodChargesForBooking(Long bookingId) {
        return roomOrderRepository.findByBookingIdOrderByCreatedAtDesc(bookingId).stream()
                .filter(o -> o.getBillingType() == RoomOrderBillingType.CHARGE_TO_ROOM)
                .map(RoomOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
