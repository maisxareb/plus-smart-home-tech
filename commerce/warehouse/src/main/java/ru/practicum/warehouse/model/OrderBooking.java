package ru.practicum.warehouse.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "order_bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderBooking {

    @Id
    @UuidGenerator
    private UUID orderId;

    @ElementCollection
    @CollectionTable(
            name = "order_products",
            joinColumns = @JoinColumn(name = "order_id")
    )
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity", nullable = false)
    private Map<UUID, Integer> products;

    @Builder.Default
    @Column(name = "delivery_weight")
    private Double deliveryWeight = 0.0;

    @Builder.Default
    @Column(name = "delivery_volume")
    private Double deliveryVolume = 0.0;

    @Builder.Default
    private Boolean fragile = false;

    @Column(name = "delivery_id")
    private UUID deliveryId;
}