package ru.practicum.warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.warehouse.model.ProductInWarehouse;

public interface WarehouseRepository extends JpaRepository<ProductInWarehouse, String> {
}