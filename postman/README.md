# Colección Postman - Product Service

Esta carpeta contiene la colección completa de Postman y las variables de entorno para probar todos los casos de uso (éxito y error) del servicio `product-service` con subida de imágenes a **Floci S3** y base de datos **H2**.

---

## Archivos incluidos

1. **[Product-Service.postman_collection.json](Product-Service.postman_collection.json)**: Colección con 11 solicitudes organizadas en 3 categorías, incluyendo scripts de validación (`pm.test`).
2. **[Product-Service.postman_environment.json](Product-Service.postman_environment.json)**: Variables de entorno (`baseUrl`, `s3Endpoint`, `bucketName`).

---

## Estructura de la Colección

### 1. Casos de Éxito (`200 OK` / `201 Created`)
- **`POST /api/products` (Fecha ISO `yyyy-MM-dd`)**: Crea el producto `laptop` y guarda la imagen con nombre `laptop_20261013.<ext>`.
- **`POST /api/products` (Fecha Compacta `yyyyMMdd`)**: Crea el producto `monitor` aceptando `20261013` como fecha válida y generando `monitor_20261013.<ext>`.
- **`GET /api/products`**: Lista todos los productos registrados.
- **`GET /api/products/:id`**: Obtiene un producto por ID con la URL directa de la imagen en S3 Floci.

### 2. Casos de Error - Validaciones de Entrada (`400 Bad Request`)
- **`POST /api/products` (Nombre Vacío)**: Valida `@NotBlank` en `nombre`.
- **`POST /api/products` (Cantidad Negativa)**: Valida `@PositiveOrZero` en `cantidad`.
- **`POST /api/products` (Monto Cero o Negativo)**: Valida `@Positive` en `monto`.
- **`POST /api/products` (Sin Imagen)**: Valida la obligatoriedad del archivo de imagen.
- **`POST /api/products` (Formato de Fecha Inválido)**: Rechaza formatos incorrectos (ej: `13-10-2026`).

### 3. Casos de Error - Reglas de Negocio (`404 Not Found` & `409 Conflict`)
- **`POST /api/products` (Conflicto por Nombre Duplicado)**: Retorna `409 Conflict` si ya existe un producto con el mismo nombre.
- **`GET /api/products/999999` (Producto Inexistente)**: Retorna `404 Not Found` en formato `ErrorResponse`.

---

## Cómo importar en Postman

1. Abre **Postman**.
2. Haz clic en el botón **Import** (esquina superior izquierda).
3. Arrastra los archivos:
   - `Product-Service.postman_collection.json`
   - `Product-Service.postman_environment.json`
4. Selecciona el entorno **`Product Service - Local Dev`** en el selector de entornos.
5. Al ejecutar los requests de tipo `POST`, asegúrate de que en la pestaña **Body -> form-data** el campo `imagen` apunte a un archivo de tu máquina (por ejemplo `C:\dev\floci\imagen-prueba.jpeg`).
