package com.example.application.product.infrastructure.mapper;

import com.example.application.product.application.dto.ProductResponse;
import com.example.application.product.domain.model.Product;
import com.example.application.product.infrastructure.persistence.ProductJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface ProductMapper {

    Product toDomain(ProductJpaEntity entity);

    ProductJpaEntity toEntity(Product product);

    @Mapping(target = "imagenUrl", ignore = true)
    ProductResponse toResponse(Product product);
}
