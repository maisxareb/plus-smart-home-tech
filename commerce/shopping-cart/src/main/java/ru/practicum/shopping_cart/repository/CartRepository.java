package ru.practicum.shopping_cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shopping_cart.model.Cart;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, String> {

    Optional<Cart> findByOwner(String owner);

}