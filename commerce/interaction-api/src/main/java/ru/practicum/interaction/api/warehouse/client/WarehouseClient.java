package ru.practicum.interaction.api.warehouse.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.interaction.api.warehouse.WarehouseOperations;

@FeignClient(name = "warehouse")
public interface WarehouseClient extends WarehouseOperations {
}