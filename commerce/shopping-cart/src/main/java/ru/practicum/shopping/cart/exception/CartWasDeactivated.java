package ru.practicum.shopping.cart.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CartWasDeactivated extends RuntimeException {
    public CartWasDeactivated(String message) {
        super(message);
    }
}