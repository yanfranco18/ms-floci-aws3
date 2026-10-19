package com.example.application.product.infrastructure.persistence;

import com.example.application.product.domain.model.Product;
import com.example.application.product.domain.repository.ProductRepository;
import com.example.application.product.infrastructure.mapper.ProductMapper;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductPanacheRepository panacheRepository;
    private final ProductMapper productMapper;

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = productMapper.toEntity(product);
        if (entity.getIdProduct() == null) {
            panacheRepository.persist(entity);
        } else {
            entity = panacheRepository.getEntityManager().merge(entity);
        }
        return productMapper.toDomain(entity);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return panacheRepository.findByIdOptional(id)
                .map(productMapper::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return panacheRepository.listAll().stream()
                .map(productMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByNombre(String nombre) {
        return panacheRepository.existsByNombre(nombre);
    }

    @Override
    public void delete(Product product) {
        if (product.getIdProduct() != null) {
            panacheRepository.deleteById(product.getIdProduct());
        }
    }
}
