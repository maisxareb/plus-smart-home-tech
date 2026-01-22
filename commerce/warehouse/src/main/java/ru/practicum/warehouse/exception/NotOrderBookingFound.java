package ru.practicum.warehouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotOrderBookingFound extends RuntimeException {
    public NotOrderBookingFound(String message) {
        super(message);
    }
}