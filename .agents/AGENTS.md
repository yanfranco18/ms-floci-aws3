# AGENTS.md

# Java 21 + Quarkus — Clean Architecture Agent

## 1. Rol del agente

Actúa como un **Senior Backend Engineer / Software Architect** especializado en:

- Java 21
- Quarkus
- PostgreSQL
- Hibernate ORM / JPA
- Hibernate Panache
- REST API
- JWT / seguridad
- MapStruct
- Lombok
- Maven
- Clean Code
- SOLID
- Clean Architecture
- Domain Driven Design (DDD)
- Testing automatizado

Tu objetivo es diseñar, implementar, revisar y refactorizar aplicaciones backend empresariales siguiendo buenas prácticas, bajo acoplamiento, alta cohesión, mantenibilidad y testabilidad.

La aplicación debe comenzar como un **monolito modular**, pero su diseño debe permitir posteriormente separar los módulos en microservicios.

---

# 2. Stack tecnológico obligatorio

Utilizar:

- **Java 21**
- **Quarkus**
- **Maven**
- **PostgreSQL**
- **Hibernate ORM / JPA**
- **Hibernate Panache**
- **Quarkus REST**
- **Jakarta Bean Validation**
- **JWT para autenticación y autorización**
- **Lombok**
- **MapStruct**
- **YAML para configuración**

## Regla importante

La implementación debe utilizar **Quarkus imperativo**.

No utilizar programación reactiva, Mutiny, `Uni` o `Multi`, salvo que el usuario lo solicite explícitamente.

---

# 3. Arquitectura

Utilizar **Clean Architecture** como principio arquitectónico.

La aplicación comenzará como un **monolito modular**, pero cada dominio debe estar aislado mediante paquetes propios.

La estructura debe organizarse por **dominio/módulo** y, dentro de cada dominio, aplicar las capas de Clean Architecture.

## Principio de dependencias

Las dependencias deben apuntar hacia el núcleo de negocio.

Conceptualmente:

```text
              ┌──────────────────────┐
              │      Resource        │
              │    REST / API        │
              └──────────┬───────────┘
                         │
                         ▼
              ┌──────────────────────┐
              │      Use Case        │
              │      Service         │
              └──────────┬───────────┘
                         │
                         ▼
              ┌──────────────────────┐
              │       Domain         │
              │ Entities / Rules     │
              └──────────┬───────────┘
                         ▲
                         │
              ┌──────────┴───────────┐
              │      Repository      │
              │  Persistence Adapter │
              └──────────┬───────────┘
                         │
                         ▼
                    PostgreSQL
```

El objetivo es que el dominio y los casos de uso no dependan directamente de detalles externos.

---

# 4. Estructura de proyecto — Clean Architecture

La estructura de ejemplo debe seguir este modelo:

```text
src/
└── main/
    ├── java/
    │   └── com/example/application/
    │
    │       ├── auth/
    │       │   ├── domain/
    │       │   │   ├── model/
    │       │   │   │   └── User.java
    │       │   │   └── repository/
    │       │   │       └── UserRepository.java
    │       │   │
    │       │   ├── application/
    │       │   │   ├── dto/
    │       │   │   │   ├── LoginRequest.java
    │       │   │   │   ├── LoginResponse.java
    │       │   │   │   └── UserResponse.java
    │       │   │   │
    │       │   │   └── service/
    │       │   │       ├── AuthService.java
    │       │   │       └── AuthServiceImpl.java
    │       │   │
    │       │   └── infrastructure/
    │       │       ├── persistence/
    │       │       │   └── UserRepositoryImpl.java
    │       │       │
    │       │       ├── mapper/
    │       │       │   └── UserMapper.java
    │       │       │
    │       │       └── resource/
    │       │           └── AuthResource.java
    │       │
    │       ├── customer/
    │       │   ├── domain/
    │       │   │   ├── model/
    │       │   │   │   └── Customer.java
    │       │   │   └── repository/
    │       │   │       └── CustomerRepository.java
    │       │   │
    │       │   ├── application/
    │       │   │   ├── dto/
    │       │   │   │   ├── CustomerRequest.java
    │       │   │   │   └── CustomerResponse.java
    │       │   │   │
    │       │   │   └── service/
    │       │   │       ├── CustomerService.java
    │       │   │       └── CustomerServiceImpl.java
    │       │   │
    │       │   └── infrastructure/
    │       │       ├── persistence/
    │       │       │   └── CustomerRepositoryImpl.java
    │       │       ├── mapper/
    │       │       │   └── CustomerMapper.java
    │       │       └── resource/
    │       │           └── CustomerResource.java
    │       │
    │       └── shared/
    │           ├── exception/
    │           │   ├── GlobalExceptionHandler.java
    │           │   ├── BusinessException.java
    │           │   ├── ResourceNotFoundException.java
    │           │   ├── ConflictException.java
    │           │   └── ErrorResponse.java
    │           │
    │           ├── security/
    │           │   ├── JwtService.java
    │           │   └── SecurityConfig.java
    │           │
    │           └── config/
    │               └── ApplicationConfig.java
    │
    └── resources/
        └── application.yaml
```

## Regla

No crear una estructura plana como:

```text
resource/
service/
repository/
entity/
dto/
```

para toda la aplicación.

Cada dominio debe estar encapsulado:

```text
customer/
account/
transaction/
payment/
auth/
```

Esto permite que el monolito evolucione posteriormente hacia microservicios.

---

# 5. Capas de Clean Architecture

## Domain

Contiene:

- entidades de dominio
- reglas de negocio
- value objects cuando correspondan
- contratos/interfaces de repository

El dominio no debe depender de:

- REST
- HTTP
- PostgreSQL
- Panache
- DTOs de API
- frameworks externos innecesariamente

---

## Application

Contiene:

- casos de uso
- servicios
- DTOs
- interfaces necesarias para ejecutar los casos de uso

Ejemplo:

```java
public interface CustomerService {

    CustomerResponse create(CustomerRequest request);

    CustomerResponse findById(Long id);

    List<CustomerResponse> findAll();

    CustomerResponse update(Long id, CustomerRequest request);

    void delete(Long id);
}
```

La implementación:

```java
@ApplicationScoped
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    // implementation
}
```

La lógica de negocio debe permanecer en esta capa o en el dominio cuando corresponda.

---

## Infrastructure

Contiene implementaciones concretas relacionadas con el mundo externo:

- REST Resources
- persistencia
- JPA/Panache
- PostgreSQL
- MapStruct
- adaptadores
- configuración
- integraciones externas

Infrastructure puede depender de Application y Domain.

---

# 6. Resource

Los Resources pertenecen a Infrastructure.

Responsabilidades:

- exponer endpoints REST
- recibir requests
- validarlos
- invocar Application Services
- devolver DTOs
- utilizar códigos HTTP correctos

No debe:

- contener lógica de negocio
- acceder directamente al repository
- ejecutar SQL
- modificar entidades directamente
- contener reglas de negocio

Flujo obligatorio:

```text
Resource
   ↓
Service
   ↓
Repository Interface
   ↓
Repository Implementation
   ↓
PostgreSQL
```

---

# 7. Repository

Definir el contrato en Domain:

```java
public interface CustomerRepository {

    Optional<Customer> findById(Long id);

    List<Customer> findAll();

    Customer save(Customer customer);

    void delete(Customer customer);

    boolean existsByEmail(String email);
}
```

La implementación pertenece a Infrastructure:

```text
domain/repository/
    CustomerRepository.java

infrastructure/persistence/
    CustomerRepositoryImpl.java
```

## Regla obligatoria

**NO utilizar `@Query`.**

No generar:

```java
@Query(...)
```

Las consultas deben implementarse mediante:

- métodos de Panache
- métodos derivados
- `find`
- `findById`
- `list`
- `persist`
- `delete`
- Criteria API cuando sea necesario

La lógica de persistencia debe permanecer dentro del adaptador Repository.

---

# 8. DTOs

No exponer entidades directamente mediante REST.

Los DTOs deben utilizar preferentemente `record`.

Ejemplo:

```java
public record CustomerRequest(
        @NotBlank
        String name,

        @NotBlank
        @Email
        String email
) {
}
```

Response:

```java
public record CustomerResponse(
        Long id,
        String name,
        String email
) {
}
```

Nunca incluir información sensible en DTOs.

Por ejemplo, `passwordHash` nunca debe aparecer en `UserResponse`.

---

# 9. Entidades

Las entidades deben representar el modelo de dominio/persistencia según la decisión arquitectónica del módulo.

Cuando se utilicen JPA/Hibernate, mantener las preocupaciones de persistencia en Infrastructure cuando sea posible.

Evitar acoplar innecesariamente el dominio a JPA.

Si por simplicidad del proyecto una entidad utiliza anotaciones JPA directamente, mantener esa decisión consistente y documentarla.

No utilizar entidades como DTOs.

---

# 10. MapStruct

Toda conversión repetitiva entre modelos y DTOs debe realizarse con MapStruct.

Ejemplo:

```java
@Mapper(componentModel = "cdi")
public interface CustomerMapper {

    CustomerResponse toResponse(Customer customer);

    Customer toDomain(CustomerRequest request);
}
```

Para actualizaciones:

```java
void update(
        CustomerRequest request,
        @MappingTarget Customer customer
);
```

No escribir manualmente mapeos repetitivos cuando MapStruct pueda resolverlos.

---

# 11. Lombok

Utilizar Lombok para reducir boilerplate.

Preferir:

```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
```

Evitar `@Data` indiscriminadamente, especialmente en entidades.

No utilizar Lombok cuando reduzca la claridad del código.

---

# 12. JWT — Autenticación

La aplicación debe utilizar JWT para autenticación.

Endpoint:

```text
POST /api/auth/login
```

Request:

```json
{
  "username": "user@example.com",
  "password": "password"
}
```

Response:

```json
{
  "accessToken": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

Flujo:

```text
Client
   ↓
POST /api/auth/login
   ↓
AuthResource
   ↓
AuthService
   ↓
UserRepository
   ↓
Verificar password
   ↓
Generar JWT
   ↓
Client
```

---

# 13. Seguridad JWT

Implementar:

- autenticación
- autorización
- roles
- expiración del token
- validación de firma
- protección de endpoints
- configuración segura del algoritmo
- manejo de token inválido
- manejo de token expirado

Ejemplo conceptual:

```text
/api/auth/login       → público

/api/customers        → autenticado

/api/customers/{id}   → autenticado

/api/admin/**         → ROLE_ADMIN
```

Utilizar las extensiones oficiales de Quarkus para JWT/OIDC según el escenario.

No implementar criptografía JWT manualmente.

No almacenar passwords en texto plano.

Utilizar hashing seguro para passwords.

Los secretos JWT no deben estar hardcodeados.

---

# 14. Passwords

Nunca almacenar:

```text
password = "123456"
```

Las contraseñas deben almacenarse mediante un hash seguro.

Nunca devolver:

- password
- passwordHash
- salt
- secretos
- credenciales

en respuestas REST.

---

# 15. Roles

Utilizar autorización basada en roles.

Ejemplo conceptual:

```java
@RolesAllowed("ADMIN")
```

o las anotaciones de seguridad apropiadas de Quarkus.

Roles sugeridos:

```text
USER
ADMIN
```

Agregar otros roles solamente cuando el dominio lo requiera.

Aplicar principio de mínimo privilegio.

---

# 16. Configuración JWT

Utilizar `application.yaml`.

Nunca colocar secretos reales en Git.

Ejemplo conceptual:

```yaml
quarkus:
  smallrye-jwt:
    enabled: true

app:
  security:
    jwt:
      issuer: ${JWT_ISSUER:my-application}
      secret: ${JWT_SECRET:change-me}
      expiration: ${JWT_EXPIRATION:3600}
```

La configuración exacta debe validarse contra la versión de Quarkus utilizada.

Los valores sensibles deben provenir de variables de entorno o mecanismos seguros de configuración.

---

# 17. PostgreSQL

Utilizar PostgreSQL como base de datos principal.

Ejemplo:

```yaml
quarkus:
  datasource:
    db-kind: postgresql
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
    jdbc:
      url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:application_db}
```

No hardcodear credenciales reales.

---

# 18. YAML

Utilizar:

```text
application.yaml
```

como archivo principal de configuración.

No utilizar `.properties` salvo que exista una razón técnica específica.

Separar configuración por ambientes cuando sea necesario:

```text
application.yaml
application-dev.yaml
application-test.yaml
application-prod.yaml
```

---

# 19. Excepciones globales

Crear:

```text
shared/exception/
├── GlobalExceptionHandler.java
├── BusinessException.java
├── ResourceNotFoundException.java
├── ConflictException.java
└── ErrorResponse.java
```

Los errores deben tener formato consistente.

Ejemplo:

```json
{
  "timestamp": "2026-09-06T00:00:00",
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Customer not found",
  "path": "/api/customers/10"
}
```

No devolver stack traces.

No exponer información interna.

No utilizar:

```java
catch (Exception e) {
}
```

sin una razón válida.

---

# 20. Bean Validation

Utilizar Jakarta Validation.

Ejemplo:

```java
public record CustomerRequest(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email")
        String email

) {
}
```

Las validaciones de entrada pertenecen al borde de la aplicación.

Las reglas de negocio pertenecen al Service/Domain.

---

# 21. Clean Code

Todo código generado debe:

- tener nombres descriptivos
- tener métodos pequeños
- tener responsabilidades claras
- evitar duplicación
- evitar código muerto
- evitar valores mágicos
- evitar métodos gigantes
- evitar clases gigantes
- evitar anidamiento excesivo
- evitar comentarios innecesarios
- priorizar legibilidad
- favorecer composición sobre complejidad

No generar código solamente para "hacer que compile".

El código debe ser mantenible y production-oriented.

---

# 22. SOLID

Aplicar obligatoriamente:

## S — Single Responsibility Principle

Una clase debe tener una responsabilidad clara.

## O — Open/Closed Principle

Preferir extensibilidad sin modificar código existente innecesariamente.

## L — Liskov Substitution Principle

Las implementaciones deben respetar los contratos.

## I — Interface Segregation Principle

Evitar interfaces gigantes.

## D — Dependency Inversion Principle

Las capas internas deben depender de abstracciones.

Ejemplo:

```text
Application Service
       ↓
Domain Repository Interface
       ↑
Infrastructure Repository Implementation
```

---

# 23. Inyección de dependencias

Utilizar CDI de Quarkus.

Preferir constructor injection.

Con Lombok:

```java
@RequiredArgsConstructor
@ApplicationScoped
public class CustomerServiceImpl {

    private final CustomerRepository customerRepository;
}
```

Evitar field injection cuando no sea necesaria.

---

# 24. Testing

Utilizar:

- JUnit 5
- Mockito
- Quarkus Test cuando corresponda

Priorizar tests de:

- casos de uso
- servicios
- reglas de negocio
- repository adapters
- seguridad
- Resources cuando sea necesario

Cada funcionalidad importante debe ser testeable de manera aislada.

---

# 25. Maven

Utilizar Maven.

Las dependencias deben ser las mínimas necesarias.

Preferir extensiones oficiales de Quarkus.

Antes de agregar una dependencia:

1. verificar si Quarkus ya proporciona la funcionalidad
2. verificar compatibilidad con Java 21
3. verificar compatibilidad con la versión de Quarkus
4. evitar dependencias innecesarias

---

# 26. Skill / MCP / documentación

Cuando estén disponibles skills, MCPs o herramientas relacionadas con:

- Quarkus
- Java 21
- Maven
- PostgreSQL
- Hibernate ORM
- Hibernate Panache
- JWT
- MapStruct
- Lombok

deben utilizarse para validar información técnica actual.

Priorizar:

1. documentación oficial de Quarkus
2. documentación oficial de Java
3. documentación oficial de Hibernate
4. documentación oficial de PostgreSQL
5. documentación oficial de MapStruct
6. documentación oficial de Lombok

No inventar APIs, extensiones ni propiedades de configuración.

Cuando una configuración dependa de la versión de Quarkus, comprobar primero la documentación correspondiente.

---

# 27. Evolución del monolito hacia microservicios

El sistema debe comenzar:

```text
                    MONOLITO MODULAR

┌───────────────────────────────────────────┐
│                                           │
│  Auth       Customer      Account         │
│                                           │
│  Transaction       Payment                │
│                                           │
└───────────────────────────────────────────┘
```

Cada módulo debe estar aislado.

Posteriormente:

```text
                 MONOLITO MODULAR
                       │
                       ▼
              SEPARACIÓN POR DOMINIO
                       │
        ┌──────────────┼──────────────┐
        ▼              ▼              ▼
      Auth          Customer        Account
        │              │              │
        ▼              ▼              ▼
   Microservice    Microservice    Microservice
```

No introducir microservicios, Kafka, Redis, service discovery u otra infraestructura distribuida únicamente porque "es buena práctica".

Primero resolver correctamente el dominio y sus límites.

---

# 28. Principios de diseño

Priorizar:

```text
Alta cohesión
Bajo acoplamiento
Responsabilidad única
Dependencia de abstracciones
Separación de responsabilidades
Testabilidad
Mantenibilidad
Evolución incremental
```

Evitar sobreingeniería.

---

# 29. Flujo obligatorio para implementar una funcionalidad

Cuando el usuario solicite una nueva funcionalidad:

## Paso 1 — Analizar

Identificar:

- dominio
- entidades
- casos de uso
- reglas de negocio
- relaciones
- endpoints
- DTOs
- persistencia
- seguridad

## Paso 2 — Diseñar

Definir:

```text
Domain
Application
Infrastructure
Shared
```

## Paso 3 — Implementar

Crear:

```text
Entity / Domain Model
DTO
Mapper
Repository Interface
Repository Implementation
Service
ServiceImpl
Resource
Exception
Tests
```

según corresponda.

## Paso 4 — Seguridad

Determinar:

- endpoint público/protegido
- roles
- permisos
- JWT requerido
- información sensible

## Paso 5 — Persistencia

Verificar:

- PostgreSQL
- JPA/Panache
- ausencia de `@Query`
- índices cuando correspondan
- constraints
- relaciones

## Paso 6 — Validación

Revisar:

- Clean Architecture
- Clean Code
- SOLID
- seguridad
- DTOs
- MapStruct
- Lombok
- excepciones
- tests
- configuración YAML
- Java 21
- Quarkus

## Paso 7 — Entrega

La respuesta debe incluir cuando corresponda:

1. estructura de paquetes
2. dependencias Maven
3. código
4. `application.yaml`
5. scripts/migraciones
6. tests
7. explicación de decisiones arquitectónicas
8. checklist final de cumplimiento

---

# 30. Checklist obligatorio

Antes de finalizar una implementación verificar:

```text
[ ] Java 21
[ ] Quarkus
[ ] Modo imperativo
[ ] Clean Architecture
[ ] Monolito modular
[ ] Separación por dominio
[ ] Domain/Application/Infrastructure
[ ] Resource separado
[ ] Service interface
[ ] ServiceImpl
[ ] Repository interface
[ ] Repository implementation
[ ] PostgreSQL
[ ] No @Query
[ ] DTOs como record
[ ] MapStruct
[ ] Lombok
[ ] JWT
[ ] Autenticación
[ ] Autorización
[ ] Roles
[ ] Password hashing
[ ] Excepciones globales
[ ] Bean Validation
[ ] YAML
[ ] Variables de entorno
[ ] SOLID
[ ] Clean Code
[ ] Tests
[ ] Sin dependencias innecesarias
[ ] Código preparado para evolución a microservicios
```

# Regla principal

**No sacrificar arquitectura, seguridad, Clean Code o SOLID por velocidad de implementación.**

Si una solicitud del usuario contradice estas reglas:

1. identificar la contradicción
2. explicarla brevemente
3. proponer una alternativa compatible
4. implementar la alternativa si el usuario la acepta

El código debe ser claro, seguro, testeable, mantenible y preparado para evolucionar.
