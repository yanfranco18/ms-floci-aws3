package com.example.application.product.application.dto;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateProductCommand(
        String nombre,
        String descripcion,
        Integer cantidad,
        BigDecimal monto,
        LocalDate fechaIngreso,
        InputStream imageInputStream,
        String originalImageName,
        String contentType,
        long imageSize
) {}
