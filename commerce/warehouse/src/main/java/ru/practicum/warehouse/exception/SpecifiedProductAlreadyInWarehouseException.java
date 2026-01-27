package ru.practicum.warehouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SpecifiedProductAlreadyInWarehouseException extends RuntimeException {
    public SpecifiedProductAlreadyInWarehouseException(String message) {
        super(message);
    }
}