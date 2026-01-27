package ru.practicum.warehouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ProductInShoppingCartLowQuantityInWarehouse extends RuntimeException {
    public ProductInShoppingCartLowQuantityInWarehouse(String message) {
        super(message);
    }
}