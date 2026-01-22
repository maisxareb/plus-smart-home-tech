package ru.practicum.order.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.order.model.Order;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByUsername(String username, Pageable pageable);

    Optional<Order> findByPaymentId(UUID paymentId);

    Optional<Order> findByDeliveryId(UUID deliveryId);
}