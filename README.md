# Sistema de Gestión de Biblioteca Universitaria — BibliotecaU

API REST backend para la administración de libros, usuarios y préstamos de una biblioteca universitaria. El sistema implementa autenticación mediante JWT, autorización basada en roles, persistencia con PostgreSQL y validaciones de negocio mediante Spring Boot.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.2.0--M2-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-blue)

**Autor:** Marcos Dionel Nicolás López
**Institución:** Kinal
**Curso:** [PENDIENTE]
**Fecha:** 7 de octubre de 2026

---

## 1. Características principales

* API REST desarrollada con Spring Boot.
* Autenticación mediante JWT.
* Arquitectura stateless con Spring Security OAuth2 Resource Server.
* Control de acceso mediante los roles `ADMIN`, `BIBLIOTECARIO` y `LECTOR`.
* Registro de usuarios como `LECTOR`.
* Inicio de sesión y generación de tokens JWT.
* Administración de libros.
* Catálogo de libros paginado.
* Validación de datos mediante Bean Validation.
* Persistencia con Spring Data JPA e Hibernate.
* PostgreSQL como sistema gestor de base de datos.
* Manejo centralizado de excepciones mediante `GlobalExceptionHandler`.
* Validación de ISBN duplicado.
* Validación de stock disponible y stock total.
* Protección contra eliminación de libros con historial de préstamos.
* Control de concurrencia mediante `@Version` y transacciones.
* Configuración de HikariCP para conexiones a PostgreSQL.
* Pruebas funcionales y pruebas de concurrencia mediante `test-api6.sh`.
* Prueba de autorización que verifica que un usuario `LECTOR` no pueda crear libros.
* Aplicación configurada para ejecutarse en el puerto `8001`.

---

## 2. Tecnologías

| Tecnología                   | Uso                                 |
| ---------------------------- | ----------------------------------- |
| Java 21                      | Lenguaje principal                  |
| Maven                        | Gestión y construcción del proyecto |
| Spring Boot 4.2.0-M2         | Framework principal                 |
| Spring Web MVC               | Desarrollo de API REST              |
| Spring Security              | Autenticación y autorización        |
| JWT / OAuth2 Resource Server | Autenticación stateless             |
| Nimbus JOSE                  | Procesamiento de JWT                |
| Spring Data JPA              | Persistencia                        |
| Hibernate 7                  | ORM                                 |
| PostgreSQL 18                | Base de datos                       |
| HikariCP                     | Pool de conexiones                  |
| Lombok                       | Reducción de código repetitivo      |
| Jakarta Bean Validation      | Validación de datos                 |
| cURL                         | Pruebas HTTP                        |
| ApacheBench                  | Pruebas de carga/concurrencia       |

---

## 3. Arquitectura

El paquete base del proyecto es:

```text
Biblioteca.BibliotecaU
```

La aplicación utiliza una arquitectura por capas:

| Capa         | Responsabilidad                                                          |
| ------------ | ------------------------------------------------------------------------ |
| `Entity`     | Representa las entidades persistentes de la base de datos.               |
| `Repository` | Acceso a datos mediante Spring Data JPA.                                 |
| `Service`    | Contiene la lógica de negocio y las transacciones.                       |
| `Controller` | Expone los endpoints REST.                                               |
| `dto`        | Objetos utilizados para las solicitudes y respuestas de la API.          |
| `Security`   | Servicios relacionados con JWT, autenticación y respuestas de seguridad. |
| `Config`     | Configuración de Spring Security, JWT y otros componentes.               |
| `Exceptions` | Excepciones de negocio y manejo centralizado de errores.                 |

### Estructura de carpetas

```text
BibliotecaU/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── Biblioteca/
│   │   │       └── BibliotecaU/
│   │   │           ├── Config/
│   │   │           ├── Controller/
│   │   │           ├── Entity/
│   │   │           ├── Exceptions/
│   │   │           ├── Repository/
│   │   │           ├── Security/
│   │   │           ├── Service/
│   │   │           ├── dto/
│   │   │           └── BibliotecaUApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── PRUEBAS/
├── Seguridad/
├── test-api6.sh
├── pom.xml
└── README.md
```

> La estructura puede contener archivos adicionales según las funcionalidades incorporadas al proyecto.

---

## 4. Requisitos previos

Para ejecutar el proyecto se requiere:

* Java JDK 21.
* Maven o Maven Wrapper.
* PostgreSQL 18.
* Git.
* cURL para ejecutar las pruebas.
* `jq` para procesar las respuestas JSON del script.
* ApacheBench (`ab`) para ejecutar la prueba de carga de 500 solicitudes y 50 conexiones concurrentes.

Comprobar Java:

```bash
java -version
```

Comprobar Maven:

```bash
mvn -version
```

Comprobar PostgreSQL:

```bash
psql --version
```

Comprobar jq:

```bash
jq --version
```

Comprobar ApacheBench:

```bash
ab -V
```

---

## 5. Instalación y configuración

### 5.1 Clonar el repositorio

```bash
git clone https://github.com/mnicolas-2025361/BibliotecaU.git
cd BibliotecaU
```

Cambiar a la rama de trabajo:

```bash
git checkout mnicolas-2025361
```

---

### 5.2 Crear la base de datos

Ingresar a PostgreSQL:

```bash
psql -U postgres
```

Crear la base de datos:

```sql
CREATE DATABASE "BibliotecaU";
```

Salir:

```sql
\q
```

> Si la base de datos ya existe, este paso no es necesario.

---

### 5.3 Configurar `application.yml`

El archivo se encuentra en:

```text
src/main/resources/application.yml
```

Ejemplo:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/BibliotecaU
    username: postgres
    password: TU_PASSWORD

    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true

server:
  port: 8001

app:
  jwt:
    secret: CAMBIAR_POR_UN_SECRETO_DE_AL_MENOS_32_CARACTERES
    expiration-ms: 3600000
```

El secreto utilizado para firmar JWT debe tener **al menos 32 caracteres**.

En producción, el secreto no debe escribirse directamente en el archivo de configuración. Debe proporcionarse mediante una variable de entorno o un sistema seguro de gestión de secretos.

---

### 5.4 Ejecutar el proyecto

Con Maven:

```bash
mvn spring-boot:run
```

O, si el Maven Wrapper está disponible:

```bash
./mvnw spring-boot:run
```

La API queda disponible en:

```text
http://localhost:8001
```

La ruta base de la API es:

```text
http://localhost:8001/api/v1
```

Al iniciar correctamente, Spring Boot debe mostrar un mensaje similar a:

```text
Tomcat started on port 8001
Started BibliotecaUApplication
```

---

## 6. Reglas de negocio

### Roles

El sistema contempla tres roles:

```text
ADMIN
BIBLIOTECARIO
LECTOR
```

### Préstamos

Las reglas definidas para préstamos son:

* No se puede prestar un libro cuando `stockDisponible` es `0`.
* Un lector puede tener como máximo **3 préstamos activos**.
* Cada préstamo tiene una duración de **14 días**.
* Si un préstamo vence sin ser devuelto, el usuario pasa a estado `SANCIONADO` al intentar realizar un nuevo préstamo.

### Libros

Para los libros se aplican las siguientes validaciones:

* El ISBN es obligatorio.
* El título es obligatorio.
* El autor es obligatorio.
* `stockTotal` no puede ser negativo.
* `stockDisponible` no puede ser negativo.
* `stockDisponible` no puede ser mayor que `stockTotal`.
* No se permite registrar un ISBN duplicado.
* No se puede eliminar un libro que tenga historial de préstamos.
* Al actualizar el stock, se consideran los ejemplares que actualmente están prestados.

Ejemplo de validación:

```java
if (r.stockDisponible() > r.stockTotal()) {
    throw new ReglaNegocioException(
            "El stock disponible no puede ser mayor que el stock total");
}
```

---

## 7. Seguridad y roles

La API utiliza JWT y mantiene las sesiones en modo stateless.

El flujo general es:

```text
Cliente
   │
   │ POST /auth/login
   ▼
API
   │
   │ valida email + contraseña
   ▼
JWT
   │
   │ Authorization: Bearer <token>
   ▼
Endpoint protegido
   │
   ├── Token válido + rol permitido → acceso
   │
   ├── Token ausente/inválido → 401
   │
   └── Rol insuficiente → 403
```

### Login

```http
POST /api/v1/auth/login
Content-Type: application/json
```

Petición:

```json
{
  "email": "admin@biblioteca.com",
  "password": "********"
}
```

Respuesta:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEnSegundos": 3600,
  "usuarioId": 2,
  "nombre": "Administrador",
  "email": "admin@biblioteca.com",
  "rol": "ADMIN"
}
```

Para acceder a un endpoint protegido:

```http
Authorization: Bearer <TOKEN>
```

### Permisos por rol

| Operación          |    ADMIN    | BIBLIOTECARIO |    LECTOR   |
| ------------------ | :---------: | :-----------: | :---------: |
| Autenticarse       |      ✓      |       ✓       |      ✓      |
| Consultar catálogo |      ✓      |       ✓       |      ✓      |
| Crear libro        |      ✓      |       ✓       |      ✗      |
| Actualizar libro   |      ✓      |       ✓       |      ✗      |
| Eliminar libro     |      ✓      |       ✓       |      ✗      |
| Crear préstamo     | [PENDIENTE] |  [PENDIENTE]  | [PENDIENTE] |
| Gestionar usuarios | [PENDIENTE] |  [PENDIENTE]  |      ✗      |

Actualmente se encuentra validado que un `LECTOR` que intenta crear un libro recibe:

```text
HTTP 403
```
