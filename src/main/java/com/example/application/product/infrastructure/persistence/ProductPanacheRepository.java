package com.example.application.product.infrastructure.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class ProductPanacheRepository implements PanacheRepositoryBase<ProductJpaEntity, Long> {

    public Optional<ProductJpaEntity> findByNombre(String nombre) {
        return find("nombre", nombre).firstResultOptional();
    }

    public boolean existsByNombre(String nombre) {
        return count("nombre", nombre) > 0;
    }
}
