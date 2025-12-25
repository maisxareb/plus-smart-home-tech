package ru.practicum.shopping_cart.expection;

public class CartWasDeactivatedException extends RuntimeException {
    public CartWasDeactivatedException(String message) {
        super(message);
    }
}