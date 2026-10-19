package com.example.application.product.infrastructure.resource;

import com.example.application.product.application.dto.CreateProductCommand;
import com.example.application.product.application.dto.ProductResponse;
import com.example.application.product.application.service.ProductService;
import com.example.application.shared.exception.BusinessException;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Tag(name = "Product", description = "Gestión de productos con almacenamiento de imágenes en S3 Floci")
public class ProductResource {

    private final ProductService productService;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Crear nuevo producto", description = "Crea un producto en la base de datos y almacena su imagen en el bucket S3 (Floci) con el nombre: nombre + fechaIngreso (ej: laptop_20261013.jpg)")
    @APIResponse(responseCode = "201", description = "Producto creado exitosamente", content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @APIResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @APIResponse(responseCode = "409", description = "Conflicto por producto duplicado")
    public Response create(@Valid @BeanParam ProductCreateForm form) {
        FileUpload fileUpload = form.getImagen();
        if (fileUpload == null || fileUpload.filePath() == null) {
            throw new BusinessException("Debe proporcionar un archivo de imagen válido");
        }

        LocalDate fechaIngreso = parseFecha(form.getFechaIngreso());

        try (InputStream inputStream = Files.newInputStream(fileUpload.filePath())) {
            CreateProductCommand command = new CreateProductCommand(
                    form.getNombre(),
                    form.getDescripcion(),
                    form.getCantidad(),
                    form.getMonto(),
                    fechaIngreso,
                    inputStream,
                    fileUpload.fileName(),
                    fileUpload.contentType(),
                    fileUpload.size());

            ProductResponse created = productService.create(command);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (IOException e) {
            throw new BusinessException("Error al leer el archivo de imagen subido: " + e.getMessage(), e);
        }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Buscar producto por ID", description = "Retorna un producto y la URL de su imagen almacenada en S3 Floci")
    @APIResponse(responseCode = "200", description = "Producto encontrado")
    @APIResponse(responseCode = "404", description = "Producto no encontrado")
    public Response findById(@PathParam("id") Long id) {
        ProductResponse product = productService.findById(id);
        return Response.ok(product).build();
    }

    @GET
    @Operation(summary = "Listar todos los productos", description = "Retorna el listado completo de productos registrados")
    @APIResponse(responseCode = "200", description = "Listado de productos")
    public Response findAll() {
        List<ProductResponse> products = productService.findAll();
        return Response.ok(products).build();
    }

    private LocalDate parseFecha(String fechaStr) {
        if (fechaStr == null || fechaStr.isBlank()) {
            throw new BusinessException("La fecha de ingreso es obligatoria");
        }
        String clean = fechaStr.trim();
        try {
            if (clean.length() == 8 && clean.chars().allMatch(Character::isDigit)) {
                return LocalDate.parse(clean, DateTimeFormatter.ofPattern("yyyyMMdd"));
            }
            return LocalDate.parse(clean, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new BusinessException("Formato de fecha inválido: '" + clean
                    + "'. Formatos aceptados: 'yyyy-MM-dd' o 'yyyyMMdd' (ej: 2026-10-13 o 20261013)");
        }
    }
}
