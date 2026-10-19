package com.example.application.product.domain.repository;

import com.example.application.product.domain.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(Long id);

    List<Product> findAll();

    boolean existsByNombre(String nombre);

    void delete(Product product);
}
