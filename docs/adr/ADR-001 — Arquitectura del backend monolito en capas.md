# ADR-001: Arquitectura del backend — monolito en capas

- **Estado:** Aceptado
- **Fecha:** 2026-10-14
- **Proyecto:** TrackFix

## Contexto

TrackFix es un backend en Java y Spring Boot para gestionar órdenes de reparación en talleres minoristas, con varios negocios (multi-negocio) y un portal público de seguimiento para el cliente.

Factores que pesan en la decisión:

- **Dominio acotado:** negocio, cliente, equipo, orden, pasos de la orden, cotización y cobro. La regla más compleja es la máquina de estados de la orden.
- **Flujos internos cortos:** cada caso de uso es una operación con una sola transacción (por ejemplo, cambiar el estado y guardar el paso en el historial).
- **Pocas integraciones externas:** solo correo y celular, y recien en la etapa 2 del producto.
- **Prioridad de entrega:** tener un MVP funcional y desplegado en 9 semanas.
- **Una sola base de datos** (PostgreSQL) y un solo punto de despliegue.

## Decisión

Se construirá un **monolito desplegable único con arquitectura en capas** (controller, service, repository), organizado por **módulos funcionales** (paquetes `orden`, `negocio`, `cliente`, etc.) y no por tipo técnico.

Reglas que se mantienen para que el diseño no se degrade:

1. Las dependencias van solo hacia abajo: controller, luego service, luego repository.
2. Los controllers trabajan con DTOs. Las entidades JPA no salen de la capa de servicio.
3. Las reglas de transición de estados viven en una clase de dominio (`Orden` o `EstadoOrden`) **sin dependencias de Spring ni de JPA**, y se prueban con tests unitarios puros.
4. Las notificaciones (Sprint 9 en adelante) se usan a través de una interfaz propia, con una implementación por canal.
5. Las reglas anteriores se verifican con pruebas de **ArchUnit**.

## Alternativas consideradas

### Arquitectura hexagonal (puertos y adaptadores)

**A favor:** dominio totalmente aislado, adaptadores intercambiables para correo y celular, pruebas muy limpias, buena señal de diseño en un portafolio.

**En contra para TrackFix:**

- Agrega puertos, casos de uso y mapeos entre dominio y persistencia. Con un solo desarrollador y sprints de 1 semana, ese costo compite directamente con entregar funcionalidad.
- Los módulos `negocio` y `cliente` son casi CRUD y casi no se benefician.
- Los beneficios principales (intercambiar canales de notificación, varias formas de entrada al sistema) no aparecen hasta la etapa 2.

**Resultado:** descartada por ahora. Se conserva lo más valioso (dominio de estados aislado y notificador tras una interfaz) con un costo mucho menor.

### Microservicios

**A favor:** escalado y despliegue independiente por servicio, equipos autónomos.

**En contra para TrackFix:**

- No hay necesidad de escalar partes de forma independiente ni varios equipos que coordinar.
- La operación **cambiar estado y guardar historial** debe ser atómica. Repartida entre servicios exigiría transacciones distribuidas o consistencia eventual, sin ningún beneficio a cambio.
- Multiplica el trabajo de infraestructura (despliegue, comunicación entre servicios, observabilidad, manejo de fallos parciales) para un solo desarrollador.
- El aislamiento multi-negocio ya se resuelve dentro de una sola base de datos con `negocio_id`.

**Resultado:** descartada. Aporta complejidad operativa que el problema no justifica.

### Comparación resumida

| Criterio | En capas (elegida) | Hexagonal | Microservicios |
| --- | --- | --- | --- |
| Velocidad para llegar al MVP | Alta | Media | Baja |
| Complejidad operativa | Baja | Baja | Alta |
| Transacción estado + historial | Simple | Simple | Difícil |
| Aíslamiento del dominio | Parcial (con reglas propias) | Total | Depende del diseño |
| Cantidad de código y mapeos | Baja | Alta | Muy alta |
| Adecuada para un solo desarrollador | Sí | Con reservas | No |

## Consecuencias

**Positivas**

- Se llega antes a un MVP completo y desplegado.
- Un solo artefacto, una sola base de datos, una transacción por caso de uso.
- Menos código que mantener, más tiempo para pruebas, seguridad y documentación.
- Las reglas de negocio clave quedan aisladas y verificables sin levantar Spring.

**Negativas y riesgos**

- Sin disciplina, la lógica puede repartirse entre servicios y entidades. Se mitiga con las reglas 1 a 5 y con ArchUnit.
- Cambiar de proveedor de notificaciones o añadir otra forma de entrada requerirá algo de refactor.
- Las entidades JPA son el modelo de persistencia, por lo que el acoplamiento con JPA es mayor que en hexagonal.

## Criterios para reconsiderar la decisión

Se revisará este ADR (creando uno nuevo que lo reemplace) si ocurre alguno de estos casos:

- La lógica de `orden` se reparte en varios servicios y ya no es posible probar las reglas sin levantar Spring.
- Al llegar al Sprint 9, la interfaz del notificador no alcanza para soportar varios canales con comodidad.
- Aparece una segunda forma de entrada al sistema con lógica propia (por ejemplo, un integrador externo).

La migración a hexagonal sería viable sin reescribir todo, porque el dominio de estados y el notificador ya están aislados. Microservicios no se contempla mientras no exista una necesidad real de escalar o desplegar partes por separado.
