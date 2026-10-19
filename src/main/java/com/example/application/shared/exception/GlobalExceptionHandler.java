package com.example.application.shared.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Throwable> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable exception) {
        String path = uriInfo != null ? uriInfo.getPath() : "";

        if (exception instanceof ResourceNotFoundException ex) {
            log.warn("Recurso no encontrado en {}: {}", path, ex.getMessage());
            return buildResponse(Response.Status.NOT_FOUND, "NOT_FOUND", ex.getMessage(), path);
        }

        if (exception instanceof ConflictException ex) {
            log.warn("Conflicto de negocio en {}: {}", path, ex.getMessage());
            return buildResponse(Response.Status.CONFLICT, "CONFLICT", ex.getMessage(), path);
        }

        if (exception instanceof BusinessException ex) {
            log.warn("Regla de negocio violada en {}: {}", path, ex.getMessage());
            return buildResponse(Response.Status.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), path);
        }

        if (exception instanceof ConstraintViolationException ex) {
            String message = ex.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            log.warn("Violación de validación en {}: {}", path, message);
            return buildResponse(Response.Status.BAD_REQUEST, "VALIDATION_ERROR", message, path);
        }

        log.error("Error no controlado en {}: {}", path, exception.getMessage(), exception);
        return buildResponse(Response.Status.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "Ocurrió un error inesperado al procesar la solicitud", path);
    }

    private Response buildResponse(Response.Status status, String error, String message, String path) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.getStatusCode(),
                error,
                message,
                path
        );
        return Response.status(status).entity(errorResponse).build();
    }
}
