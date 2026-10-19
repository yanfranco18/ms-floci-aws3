package com.example.application.product.infrastructure.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductCreateForm {

    @RestForm("nombre")
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @RestForm("descripcion")
    private String descripcion;

    @RestForm("cantidad")
    @NotNull(message = "La cantidad es obligatoria")
    @PositiveOrZero(message = "La cantidad no puede ser negativa")
    private Integer cantidad;

    @RestForm("monto")
    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    private BigDecimal monto;

    @RestForm("fechaIngreso")
    @NotBlank(message = "La fecha de ingreso es obligatoria")
    private String fechaIngreso;

    @RestForm("imagen")
    @NotNull(message = "El archivo de imagen es obligatorio")
    private FileUpload imagen;
}
