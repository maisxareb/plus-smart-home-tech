package ru.practicum.warehouse.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.interaction.api.warehouse.dto.DimensionDto;

@Entity
@Table(name = "warehouse_products")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductInWarehouse {

    @Id
    @Column(name = "product_id")
    private String productId;

    @Column(nullable = false)
    private Boolean fragile;

    @Embedded
    @Column(nullable = false)
    private DimensionDto dimension;

    @Column(nullable = false)
    private Double weight;

    @Builder.Default
    private Integer quantity = 0;
}