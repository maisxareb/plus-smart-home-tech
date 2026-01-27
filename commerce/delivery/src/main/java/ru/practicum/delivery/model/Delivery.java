package ru.practicum.delivery.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import ru.practicum.interaction.api.delivery.dto.DeliveryState;

import java.util.UUID;

@Entity
@Table(name = "deliveries")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Delivery {

    @Id
    @UuidGenerator
    @Column(name = "delivery_id")
    private UUID deliveryId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="country", column=@Column(name="from_country", nullable = false)),
            @AttributeOverride(name="city", column=@Column(name="from_city", nullable = false)),
            @AttributeOverride(name="street", column=@Column(name="from_street", nullable = false)),
            @AttributeOverride(name="house", column=@Column(name="from_house", nullable = false)),
            @AttributeOverride(name="flat", column=@Column(name="from_flat"))
    })
    private DeliveryAddress fromAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="country", column=@Column(name="to_country", nullable = false)),
            @AttributeOverride(name="city", column=@Column(name="to_city", nullable = false)),
            @AttributeOverride(name="street", column=@Column(name="to_street", nullable = false)),
            @AttributeOverride(name="house", column=@Column(name="to_house", nullable = false)),
            @AttributeOverride(name="flat", column=@Column(name="to_flat"))
    })
    private DeliveryAddress toAddress;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_state", nullable = false)
    @Builder.Default
    private DeliveryState deliveryState = DeliveryState.CREATED;
}