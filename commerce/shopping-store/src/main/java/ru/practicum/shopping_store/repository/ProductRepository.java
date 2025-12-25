package ru.practicum.shopping_store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shopping_store.model.Product;
import ru.practicum.shopping_store.model.ProductCategory;

public interface ProductRepository extends JpaRepository<Product, String> {

    Page<Product> findAllByProductCategory(ProductCategory category, Pageable pageable);
}