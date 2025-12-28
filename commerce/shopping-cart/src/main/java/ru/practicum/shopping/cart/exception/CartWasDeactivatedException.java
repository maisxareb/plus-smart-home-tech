package ru.practicum.shopping.cart.exception;

public class CartWasDeactivatedException extends RuntimeException {
    public CartWasDeactivatedException(String message) {
        super(message);
    }
}