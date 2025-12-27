package ru.practicum.interaction_api.warehouse.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.interaction_api.warehouse.WarehouseOperations;

@FeignClient(name = "warehouse")
public interface WarehouseClient extends WarehouseOperations {
}