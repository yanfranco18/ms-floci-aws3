package com.example.application.product.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductResponse(
        Long idProduct,
        String nombre,
        String descripcion,
        Integer cantidad,
        BigDecimal monto,
        LocalDate fechaIngreso,
        String imagen,
        String imagenUrl
) {}
