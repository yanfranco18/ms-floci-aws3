package com.example.application.product.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private Long idProduct;
    private String nombre;
    private String descripcion;
    private Integer cantidad;
    private BigDecimal monto;
    private LocalDate fechaIngreso;
    private String imagen;

    /**
     * Genera el nombre del archivo de imagen con formato:
     * nombre + "_" + fechaIngreso (yyyyMMdd) + extension
     * Ejemplo: laptop_20261013.jpg
     */
    public static String generateImageFileName(String nombre, LocalDate fechaIngreso, String originalFileName) {
        String cleanNombre = (nombre != null ? nombre.trim().toLowerCase() : "producto")
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-z0-9_-]", "");

        if (cleanNombre.isBlank()) {
            cleanNombre = "producto";
        }

        String fechaStr = fechaIngreso != null
                ? fechaIngreso.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
        }
        if (extension.isBlank()) {
            extension = ".jpg";
        }

        return cleanNombre + "_" + fechaStr + extension;
    }
}
