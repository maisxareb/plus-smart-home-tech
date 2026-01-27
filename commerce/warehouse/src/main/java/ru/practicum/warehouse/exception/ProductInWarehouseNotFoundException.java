package ru.practicum.warehouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductInWarehouseNotFoundException extends RuntimeException {
    public ProductInWarehouseNotFoundException(String message) {
        super(message);
    }
}