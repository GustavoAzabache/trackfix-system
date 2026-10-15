# TrackFix

Estado: en desarrollo

Este proyecto aún está en desarrollo. Algunas funcionalidades pueden cambiar o no estar disponibles todavía.

## ¿Qué es?

TrackFix es una aplicación web multi-taller para gestionar reparaciones de equipos electrónicos en talleres minoristas. Registra la recepción de cada equipo, sigue la orden por estados con historial completo y entrega al cliente un código de seguimiento para consultar el avance y aprobar o rechazar cotizaciones.

## ¿Qué problema resuelve?

- Control informal de órdenes en cuadernos, WhatsApp o de memoria, con pérdida o mezcla de equipos.
- Interrupciones constantes al técnico por clientes que preguntan si su equipo ya está listo.
- Falta de evidencia al recibir el equipo y de constancia en precios, plazos y aprobaciones.
- Ausencia de historial por orden y de gestión de equipos abandonados o pendientes de recoger.

Pensado para técnicos y talleres minoristas en Perú (1 a 5 personas) y sus clientes finales.

## Tecnologías

Java 25, Spring Boot 4.1.1, Spring Web MVC, Spring Data JPA, Spring Security, Bean Validation, PostgreSQL, Lombok, Maven.

## Estructura del proyecto

```text
trackfix-system/
├── backend-springboot/
│   ├── src/main/java/com/me/backend_springboot/
│   ├── src/main/resources/application.yaml
│   ├── src/test/
│   ├── pom.xml
│   ├── mvnw y mvnw.cmd
├── docs/
│   ├── prd/
│   ├── adr/
│   ├── db/
```

- `backend-springboot/`: backend Spring Boot (único módulo implementado hasta ahora).
- `backend-springboot/src/main/java/com/me/backend_springboot/`: punto de entrada (`BackendSpringbootApplication`).
- `backend-springboot/src/main/resources/application.yaml`: configuración de datasource JPA y PostgreSQL.
- `docs/prd/`: documento de requisitos del producto.
- `docs/adr/`: decisiones de arquitectura (ADR-001: monolito en capas).
- `docs/db/`: carpeta reservada para diseño de base de datos (vacía por ahora).

## Requisitos previos

- Java 25
- Maven (el repo incluye el wrapper `mvnw`, no requiere instalación global)
- PostgreSQL en local con una base de datos llamada `trackfix_system`

Configuración actual en `backend-springboot/src/main/resources/application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/trackfix_system
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: create-drop
```

## Cómo clonarlo

No hay remote de git configurado en este entorno, entra al directorio del proyecto:

```bash
cd trackfix-system
```

## Cómo levantarlo

1. Crear la base de datos vacía en PostgreSQL:

```bash
createdb -h localhost -U postgres trackfix_system
```

2. Arrancar el backend en desarrollo desde la carpeta del backend:

```bash
cd backend-springboot
./mvnw spring-boot:run
```

Sin `server.port` configurado en `application.yaml`, Spring Boot usa su puerto por defecto (8080).

## Cómo probarlo

El proyecto incluye el test base de contexto generado por Spring Boot (`contextLoads`).

```bash
cd backend-springboot
./mvnw test
```
