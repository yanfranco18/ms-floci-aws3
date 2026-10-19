package com.example.application.product.application.service;

import com.example.application.product.application.dto.CreateProductCommand;
import com.example.application.product.application.dto.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse create(CreateProductCommand command);

    ProductResponse findById(Long id);

    List<ProductResponse> findAll();
}
