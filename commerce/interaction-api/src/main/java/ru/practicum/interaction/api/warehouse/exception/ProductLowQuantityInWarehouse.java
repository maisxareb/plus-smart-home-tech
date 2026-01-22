package ru.practicum.interaction.api.warehouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ProductLowQuantityInWarehouse extends RuntimeException {
    public ProductLowQuantityInWarehouse(String message) {
        super(message);
    }
}