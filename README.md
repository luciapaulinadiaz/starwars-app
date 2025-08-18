# Star Wars API Challenge

Una aplicación Java 8 Spring Boot que se integra con la API de Star Wars (SWAPI) para proporcionar acceso autenticado a información sobre personajes, películas, naves espaciales y vehículos del universo Star Wars.

## Tabla de Contenidos

- [Características](#características)
- [Tecnologías](#tecnologías)
- [Demo en Vivo](#demo-en-vivo)
- [Instalación Local](#instalación-local)
- [Uso](#uso)
- [API Endpoints](#api-endpoints)
- [Autenticación](#autenticación)
- [Testing](#testing)
- [Arquitectura](#arquitectura)
- [Configuración](#configuración)

## Características

- Integración completa con SWAPI: Acceso a People, Films, Starships y Vehicles
- Autenticación JWT: Sistema seguro de login y registro
- Paginación: Listados paginados para manejo eficiente de datos
- Filtrado por ID: Búsqueda específica de entidades
- Documentación Swagger: API docs interactiva
- Testing completo: +60 tests unitarios y de integración (80%+ coverage)
- Base de datos H2: Configuración lista para desarrollo
- Manejo de errores global: Respuestas consistentes y detalladas

## Tecnologías

- Java 8
- Spring Boot 2.7.17
- Spring Security (JWT Authentication)
- Spring Data JPA
- H2 Database
- Maven
- JUnit 5 & Mockito
- Swagger/OpenAPI 3
- JaCoCo (Code Coverage)
- Lombok

## Demo en Vivo

**Aplicación desplegada en Railway**: [https://starwars-app-production-9911.up.railway.app](https://starwars-app-production-9911.up.railway.app)

**Documentación interactiva**: [Ver API Docs](https://starwars-app-production-9911.up.railway.app/swagger-ui.html)

## Instalación Local

### Prerrequisitos

- Java 8
- Maven 3.6+ (o usar el wrapper incluido)

### Pasos de instalación

```bash
# 1. Clonar el repositorio
git clone https://github.com/luciapaulinadiaz/starwars-app.git
cd starwars-app

# 2. Ejecutar la aplicación
./mvnw spring-boot:run

# La aplicación estará disponible en: http://localhost:8080
```

## Uso

### Explorar la API

1. **Swagger UI**: `http://localhost:8080/swagger-ui.html`
2. **H2 Console**: `http://localhost:8080/h2-console`
    - JDBC URL: `jdbc:h2:mem:starwarsdb`
    - Username: `sa`
    - Password: (vacío)

### Flujo de autenticación

1. Registrarse para obtener token JWT
2. Usar token en header `Authorization: Bearer <token>`
3. Explorar endpoints de Star Wars

## API Endpoints

### Autenticación

```http
POST /api/auth/register    # Registrar nuevo usuario
POST /api/auth/login       # Iniciar sesión
GET  /api/auth/validate    # Validar token JWT
```

### Star Wars Data (Requiere autenticación)

```http
# People (Personajes)
GET /api/people?page=1&limit=10
GET /api/people/{id}
GET /api/people/health

# Films (Películas)
GET /api/films?page=1&limit=10
GET /api/films/{id}
GET /api/films/health

# Starships (Naves espaciales)
GET /api/starships?page=1&limit=10
GET /api/starships/{id}
GET /api/starships/health

# Vehicles (Vehículos)
GET /api/vehicles?page=1&limit=10
GET /api/vehicles/{id}
GET /api/vehicles/health
```

## Autenticación

### Ejemplo: Registro de Usuario

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "luke_skywalker",
    "email": "luke@rebellion.com",
    "password": "force123",
    "firstName": "Luke",
    "lastName": "Skywalker"
  }'
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "user": {
    "id": 1,
    "username": "luke_skywalker",
    "email": "luke@rebellion.com",
    "firstName": "Luke",
    "lastName": "Skywalker",
    "role": "USER"
  }
}
```

### Ejemplo: Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "luke_skywalker",
    "password": "force123"
  }'
```

### Ejemplo: Usar Token

```bash
curl -X GET http://localhost:8080/api/people \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Testing

### Ejecutar Tests

```bash
# Todos los tests
./mvnw test

# Con reporte de cobertura
./mvnw clean test jacoco:report
```

### Coverage Report

Después de ejecutar tests con JaCoCo, el reporte estará en: `target/site/jacoco/index.html`

### Test Stats

- 60+ archivos de test
- Tests unitarios con Mockito
- Tests de integración para controllers
- Tests de seguridad JWT
- Tests de validación y excepciones

## Arquitectura

### Estructura del Proyecto

```
src/main/java/com/starwars/app/
├── config/          # Configuraciones (Security, RestTemplate, etc.)
├── controller/      # REST Controllers  
├── dto/             # Data Transfer Objects
├── entity/          # JPA Entities
├── exception/       # Global Exception Handlers
├── repository/      # JPA Repositories
├── security/        # JWT Security Components
└── service/         # Business Logic
    └── external/    # External API Services
```





