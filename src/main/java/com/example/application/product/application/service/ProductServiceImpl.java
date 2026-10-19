package com.example.application.product.application.service;

import com.example.application.product.application.dto.CreateProductCommand;
import com.example.application.product.application.dto.ProductResponse;
import com.example.application.product.domain.model.Product;
import com.example.application.product.domain.repository.ProductRepository;
import com.example.application.product.infrastructure.mapper.ProductMapper;
import com.example.application.shared.exception.ConflictException;
import com.example.application.shared.exception.ResourceNotFoundException;
import com.example.application.shared.storage.FileStorageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse create(CreateProductCommand command) {
        if (productRepository.existsByNombre(command.nombre().trim())) {
            throw new ConflictException("Ya existe un producto con el nombre: " + command.nombre());
        }

        // Generar nombre de imagen según regla: nombre + fechaIngreso (ejemplo: laptop_20261013.jpg)
        String fileName = Product.generateImageFileName(
                command.nombre(),
                command.fechaIngreso(),
                command.originalImageName());

        // Subir archivo al bucket S3 Floci
        String savedFileName = fileStorageService.uploadFile(
                command.imageInputStream(),
                fileName,
                command.contentType(),
                command.imageSize());

        Product product = Product.builder()
                .nombre(command.nombre().trim())
                .descripcion(command.descripcion() != null ? command.descripcion().trim() : null)
                .cantidad(command.cantidad())
                .monto(command.monto())
                .fechaIngreso(command.fechaIngreso())
                .imagen(savedFileName)
                .build();

        try {
            Product savedProduct = productRepository.save(product);
            log.info("Producto creado exitosamente con ID: {} y nombre: '{}'", savedProduct.getIdProduct(),
                    savedProduct.getNombre());
            return toResponseWithUrl(savedProduct);
        } catch (Exception e) {
            log.error("Error al persistir el producto en la BD. Revirtiendo imagen subida a S3 '{}': {}", savedFileName,
                    e.getMessage());
            try {
                fileStorageService.deleteFile(savedFileName);
            } catch (Exception deleteEx) {
                log.warn("No se pudo eliminar el archivo huérfano de S3 '{}': {}", savedFileName,
                        deleteEx.getMessage());
            }
            throw e;
        }
    }

    @Override
    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        return toResponseWithUrl(product);
    }

    @Override
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(this::toResponseWithUrl)
                .toList();
    }

    private ProductResponse toResponseWithUrl(Product product) {
        ProductResponse res = productMapper.toResponse(product);
        String url = fileStorageService.getFileUrl(product.getImagen());
        return new ProductResponse(
                res.idProduct(),
                res.nombre(),
                res.descripcion(),
                res.cantidad(),
                res.monto(),
                res.fechaIngreso(),
                res.imagen(),
                url);
    }
}
