# TrackFix — Documento de requisitos de producto (PRD)

Versión 1.0 · 14 de octubre de 2026 · Proyecto personal de portafolio (Java + Spring Boot)

## 1. Resumen

TrackFix es una aplicación web **multi-taller** para gestionar reparaciones de equipos electrónicos en talleres minoristas. Registra la recepción de cada equipo, lleva la orden por una serie de estados con **trazabilidad completa** (qué pasó, cuándo y quién lo hizo) y le entrega al cliente un **código de seguimiento** con el que consulta el avance y aprueba o rechaza cotizaciones sin tener que llamar al taller.

El proyecto tiene un doble objetivo:

- **Producto:** una herramienta realmente útil para técnicos minoristas en Perú.
- **Portafolio:** demostrar diseño de dominio, seguridad, multi-tenancy y calidad de ingeniería en Java y Spring Boot.

## 2. Problema que soluciona

Las siguientes son **hipótesis a validar** conversando con técnicos reales antes de dar el producto por cerrado:

- **Control informal.** Las órdenes se llevan en cuadernos, hojas sueltas, WhatsApp o de memoria. Es fácil perder datos o mezclar equipos.
- **Clientes que preguntan todo el día.** El técnico interrumpe la reparación para responder si el equipo ya está listo.
- **Sin evidencia al recibir el equipo.** Cuando el cliente reclama que la pantalla ya estaba rota o que faltaba un accesorio, no hay constancia.
- **Aprobaciones verbales.** El precio y el plazo se acuerdan de palabra y luego se discuten.
- **Sin historial.** No queda registro de quién revisó el equipo, cuándo y qué encontró.
- **Equipos abandonados.** Equipos rechazados o ya reparados que nadie recoge y que nadie vuelve a gestionar.

**Propuesta de valor**

| Para | Beneficio |
| --- | --- |
| El taller | Orden y evidencia por cada equipo, menos llamadas, aprobaciones con constancia, imagen más profesional |
| El cliente | Sabe en qué etapa está su equipo en cualquier momento, y decide sobre el precio con la información completa |

## 3. Público objetivo

**Primario:** técnicos y talleres minoristas en Perú. Suelen ser independientes o equipos de 1 a 5 personas, instalados en galerías, locales de barrio o tiendas pequeñas, que reparan laptops, celulares, tablets, consolas y otros dispositivos.

**Rasgos relevantes para el diseño**

- Usan mucho el celular, así que la interfaz debe ser **mobile-first**.
- Tienen poco tiempo y poca paciencia para software complejo: el registro de una recepción debe ser rápido (objetivo: menos de 2 minutos).
- WhatsApp es su canal principal con los clientes.
- Cobran en efectivo y con billeteras digitales.

**Usuarios del sistema**

| Rol | Tiene cuenta | Acciones clave |
| --- | --- | --- |
| Administrador del taller | Sí | Configurar el taller, crear técnicos, ver todas las órdenes, ver reportes |
| Técnico | Sí | Recepcionar equipos, revisar, cotizar, reparar, registrar cobro y entrega |
| Cliente final | No (usa el código) | Consultar el estado, ver la cotización, aprobarla o rechazarla |

## 4. Alcance

|  | MVP | Producto final |
| --- | --- | --- |
| Multi-taller con datos aislados | Sí | Sí |
| Autenticación y roles (admin, técnico) | Sí | Sí |
| Registro de clientes y equipos (cualquier tipo de dispositivo) | Sí | Sí |
| Recepción con código de seguimiento mostrado **en pantalla** | Sí | Sí |
| Estados con historial completo | Sí | Sí |
| Diagnóstico y cotización (problema, precio, tiempo estimado) | Sí | Sí |
| Portal público de seguimiento con aprobar o rechazar | Sí | Sí |
| Registro de cobro (monto y método de pago) y entrega | Sí | Sí |
| Envío del código y de los cambios de estado por **correo o celular**, a elección del cliente | No | Sí |
| Comprobante de recepción en PDF con QR | No | Sí |
| Expiración y recordatorio de cotizaciones sin respuesta | No | Sí |
| Fotos del equipo en la recepción | No | Sí |
| Reportes y dashboard | No | Sí |

**Fuera de alcance (por ahora):** facturación electrónica, inventario de repuestos, pasarela de pagos en línea, aplicación móvil nativa.

## 5. Entradas y salidas

### Entradas

| Entrada | Datos |
| --- | --- |
| Taller y usuarios | Nombre del taller, datos de contacto, usuarios con nombre, correo, contraseña y rol |
| Cliente | Nombre, documento de identidad (por definir si es obligatorio), teléfono, correo, canal de notificación preferido (producto final) |
| Equipo | Tipo (laptop, celular, tablet, consola, otro), marca, modelo, número de serie o IMEI, color, accesorios entregados, estado físico al recibir |
| Recepción | Falla reportada por el cliente, observaciones, fecha y hora, técnico que recepciona |
| Revisión | Comentarios del técnico, cambio de estado |
| Diagnóstico y cotización | Problema encontrado, descripción, precio, tiempo estimado |
| Decisión del cliente | Aprobar o rechazar, con motivo opcional |
| Cobro | Monto pagado, método de pago, fecha |
| Consulta pública | Código de seguimiento |

### Salidas

| Salida | Detalle |
| --- | --- |
| Código de seguimiento | Se muestra en pantalla al terminar la recepción. En el producto final también se envía por correo o celular |
| Línea de tiempo | Cada estado con fecha, hora y responsable (ejemplo: «Recepcionado el 14/10 a las 10:32 por Carlos») |
| Vista del cliente | Estado actual, línea de tiempo y cotización pendiente, sin datos sensibles |
| Panel del técnico | Lista de órdenes con filtros por estado, cliente o código |
| Detalle de orden | Datos del equipo, historial, cotizaciones y cobro |
| Comprobante de recepción | PDF con el código y un QR (producto final) |
| Notificaciones | Correo o mensaje al celular en cada cambio de estado (producto final) |
| Reportes | Órdenes por estado, tiempo promedio por etapa, ingresos por periodo y método de pago (producto final) |
| Registro de auditoría | Quién hizo qué y cuándo, por orden |

## 6. Estados y transiciones

Cada cambio de estado crea un registro nuevo e inmutable en el historial. No se permiten saltos fuera de las transiciones de esta tabla.

| Desde | Hacia | Quién lo dispara |
| --- | --- | --- |
| (inicio) | Recepcionado | Técnico |
| Recepcionado | En revisión | Técnico |
| En revisión | Esperando aprobación (problema encontrado, con precio y tiempo) | Técnico |
| Esperando aprobación | Aprobado | Cliente |
| Esperando aprobación | Rechazado | Cliente |
| Rechazado | Pendiente de recoger | Sistema (automático) |
| Aprobado | En reparación | Técnico |
| En reparación | Esperando aprobación (nuevo hallazgo con nueva cotización) | Técnico |
| En reparación | Listo para recoger | Técnico |
| Listo para recoger | Entregado (requiere cobro registrado) | Técnico |
| Pendiente de recoger | Entregado | Técnico |

**Código de seguimiento:** formato tipo `TF-8K3M-2QX9`, generado de forma aleatoria segura, único, sin caracteres ambiguos (0/O, 1/I) y no adivinable. Nunca se usa el ID interno.

## 7. Requisitos funcionales

- **RF-01.** Registrar un taller con su administrador inicial.
- **RF-02.** Autenticación con JWT y autorización por rol.
- **RF-03.** Aislamiento total de datos entre talleres: ningún usuario puede ver ni modificar datos de otro taller.
- **RF-04.** El administrador crea y desactiva técnicos.
- **RF-05.** Registrar y buscar clientes por documento, nombre o teléfono.
- **RF-06.** Registrar equipos de cualquier tipo con sus datos de identificación y estado físico.
- **RF-07.** Recepcionar un equipo en un solo flujo (cliente, equipo, falla) y generar el código de seguimiento.
- **RF-08.** Mostrar el código en pantalla inmediatamente después de la recepción.
- **RF-09.** Cambiar el estado de una orden validando las transiciones permitidas.
- **RF-10.** Guardar un historial inmutable de estados con fecha, hora, responsable y comentario.
- **RF-11.** Registrar el diagnóstico con problema, precio y tiempo estimado, y pasar la orden a espera de aprobación.
- **RF-12.** Permitir una nueva cotización si aparece otro problema durante la reparación, conservando el histórico de cotizaciones.
- **RF-13.** Portal público: consultar la orden por código, sin iniciar sesión.
- **RF-14.** Portal público: aprobar o rechazar la cotización vigente.
- **RF-15.** Al rechazar, crear automáticamente los estados Rechazado y Pendiente de recoger.
- **RF-16.** Registrar el cobro (monto y método de pago) y exigirlo antes de marcar la orden como Entregada.
- **RF-17.** Listar y filtrar órdenes del taller por estado, cliente y fechas.
- **RF-18 (final).** Enviar el código y los cambios de estado por el canal elegido por el cliente (correo o celular).
- **RF-19 (final).** Generar el comprobante de recepción en PDF con QR.
- **RF-20 (final).** Expirar o recordar cotizaciones sin respuesta y equipos pendientes de recoger.
- **RF-21 (final).** Adjuntar fotos del equipo a la recepción.
- **RF-22 (final).** Dashboard con reportes básicos.

## 8. Requisitos no funcionales

- **Seguridad del portal público:** código no adivinable, límite de peticiones por IP, y solo los datos mínimos necesarios (sin documento ni teléfono completo).
- **Seguridad general:** contraseñas con hash, JWT con expiración, validación de entradas, y datos de otros talleres inaccesibles incluso por manipulación de IDs.
- **Usabilidad:** diseño mobile-first, interfaz en español, flujos de recepción y consulta cortos.
- **Trazabilidad:** historial inmutable y registro de auditoría.
- **Fecha y hora:** se guardan en UTC y se muestran en la zona horaria America/Lima.
- **Calidad:** pruebas unitarias de reglas de negocio, pruebas de integración con base de datos real, y un test que verifique el aislamiento entre talleres.
- **Operación:** todo el sistema se levanta con un solo comando (Docker Compose), con migraciones versionadas y pipeline de CI.
- **Documentación:** API documentada con OpenAPI/Swagger y un README con instrucciones, decisiones técnicas y capturas.

## 9. Stack tentativo

| Capa | Tecnología |
| --- | --- |
| Backend | Java 21 (LTS), Spring Boot (última versión estable), Spring Web, Spring Data JPA, Spring Security + JWT |
| Base de datos | PostgreSQL con migraciones Flyway |
| Mapeo y validación | MapStruct, Bean Validation |
| Pruebas | JUnit 5, Mockito, Testcontainers |
| Documentación | springdoc-openapi (Swagger UI) |
| Frontend | React o Angular (por decidir), diseño responsive |
| Infraestructura | Docker Compose, GitHub Actions, despliegue de demo en la nube |

**Multi-taller:** base de datos y esquema compartidos, con una columna `taller_id` en cada tabla de negocio y filtrado automático en la capa de acceso a datos. Es la opción más simple de construir y demostrar; queda como decisión técnica a confirmar.

## 10. Plan por sprints

Cada sprint dura **1 semana** y está dimensionado para pocas horas diarias. Cada uno entrega un tramo vertical de funcionalidad (backend y frontend) que se pueda probar de punta a punta. Si algo no cabe, pasa al siguiente sprint sin cambiar el orden.

**Definición de terminado para cualquier sprint:** funcionalidad probada, pruebas automatizadas de las reglas nuevas, Swagger actualizado, CI en verde y README al día.

### Etapa 1: MVP (Sprints 0 a 8)

**Sprint 0 — Cimientos**

- Repositorio(s), proyecto Spring Boot en capas (controller, service, repository), DTOs.
- PostgreSQL, Flyway y Docker Compose.
- Manejo global de excepciones con respuestas de error consistentes.
- Swagger y CI básico con GitHub Actions.
- Frontend inicializado con enrutamiento y pantalla de login vacía.
- *Terminado cuando:* un solo comando levanta backend, base de datos y frontend, y el CI compila.

**Sprint 1 — Talleres y autenticación**

- Entidades Taller y Usuario, con roles Administrador y Técnico.
- Registro de taller con su administrador, login con JWT y creación de técnicos.
- Aislamiento por `taller_id` aplicado de forma automática.
- Frontend: login y layout protegido por rol.
- *Terminado cuando:* un test automatizado demuestra que un usuario del taller A no accede a datos del taller B.

**Sprint 2 — Clientes y equipos**

- CRUD de clientes con búsqueda por documento, nombre y teléfono.
- Equipos con tipo (laptop, celular, tablet, consola, otro), marca, modelo, serie o IMEI, accesorios y estado físico.
- Frontend: formularios y listados.

**Sprint 3 — Recepción y código de seguimiento**

- Orden de reparación y flujo de recepción en una sola pantalla (cliente, equipo, falla reportada).
- Generador de código de seguimiento único y no adivinable.
- Estado inicial Recepcionado con su primer registro de historial.
- Frontend: al terminar la recepción se muestra el código en pantalla.
- *Terminado cuando:* se puede recepcionar un equipo y obtener un código válido.

**Sprint 4 — Estados e historial**

- Máquina de estados con transiciones validadas (enum y reglas propias).
- Historial inmutable con estado, fecha, hora, usuario y comentario.
- Panel de órdenes con filtros por estado y detalle con línea de tiempo.
- Pruebas unitarias de todas las transiciones válidas e inválidas.

**Sprint 5 — Diagnóstico y cotización**

- Paso a En revisión.
- Registro de problema encontrado, precio y tiempo estimado, que lleva la orden a Esperando aprobación.
- Soporte de nueva cotización por segundo hallazgo, conservando las anteriores.
- Frontend: formulario de diagnóstico y vista de cotizaciones.

**Sprint 6 — Portal público del cliente**

- Consulta por código sin login, con línea de tiempo en lenguaje amigable.
- Vista de la cotización pendiente y botones de aprobar o rechazar.
- Rechazo que genera Rechazado y Pendiente de recoger automáticamente.
- Límite de peticiones por IP y exposición mínima de datos.
- Frontend público, responsive y pensado para el celular.

**Sprint 7 — Reparación, cobro y entrega**

- Flujo Aprobado, En reparación, Listo para recoger.
- Registro de cobro (monto, método de pago, fecha).
- Entregado solo con cobro registrado, o desde Pendiente de recoger según la regla definida.
- Frontend: pantallas de reparación, cobro y entrega.

**Sprint 8 — Cierre del MVP**

- Pruebas de integración con Testcontainers y revisión de cobertura.
- Datos de ejemplo (seed) para la demo, revisión de seguridad y pulido de la interfaz.
- README completo, Swagger revisado y despliegue de demo en vivo.
- *Terminado cuando:* una persona ajena puede hacer el recorrido completo (recepción, cotización, aprobación, cobro y entrega) desde el celular.

### Etapa 2: Producto final (Sprints 9 a 13)

**Sprint 9 — Notificaciones por correo**

- Preferencia de canal por cliente.
- Envío asíncrono (`@Async` o cola) del código y de cada cambio de estado, con plantillas y reintentos.
- Registro de notificaciones enviadas.

**Sprint 10 — Notificaciones al celular**

- Abstracción de canal de notificación para poder cambiar de proveedor.
- Integración con el canal elegido para celular (SMS o WhatsApp, por decidir).

**Sprint 11 — Comprobante y vencimientos**

- Comprobante de recepción en PDF con QR del código.
- Tarea programada (`@Scheduled`) para vencer o recordar cotizaciones sin respuesta y equipos pendientes de recoger.

**Sprint 12 — Evidencia fotográfica**

- Subida de fotos del equipo en la recepción y almacenamiento de archivos.
- Visualización en el detalle de la orden.

**Sprint 13 — Reportes y endurecimiento**

- Dashboard: órdenes por estado, tiempo promedio por etapa, ingresos por periodo y método de pago.
- Refuerzo de seguridad multi-taller, observabilidad básica (logs estructurados, Actuator) y pulido final.

**Duración total estimada:** 9 semanas para el MVP y 14 semanas para el producto final.

## 11. Criterios de éxito

**Como producto**

- Un técnico recepciona un equipo y obtiene el código en menos de 2 minutos.
- Un cliente consulta el estado desde su celular sin crear cuenta.
- Al menos un técnico real prueba la demo y da retroalimentación.

**Como proyecto de portafolio**

- Demo en vivo y repositorio con README profesional.
- Pruebas automatizadas de las reglas de negocio, incluido el aislamiento entre talleres.
- Historial de commits y sprints ordenado, que cuente la evolución del proyecto.

## 12. Riesgos

| Riesgo | Mitigación |
| --- | --- |
| Alcance demasiado grande para sprints de 1 semana | Priorizar el MVP; la etapa 2 se puede recortar sin perder el valor del proyecto |
| Frontend consume más tiempo del esperado | Interfaz simple y componentes reutilizables; backend primero en cada sprint |
| Datos sensibles de clientes | Exponer lo mínimo en el portal público y no guardar datos innecesarios |
| Costo de canales de celular (SMS o WhatsApp) | Dejarlos para la etapa 2 y aislarlos tras una abstracción |

## 13. Decisiones abiertas

No se asumió nada de lo siguiente. Hay que definirlo antes o durante los sprints indicados.

1. **Frontend:** ¿React o Angular? (Sprint 0)
2. **Repositorios:** ¿uno solo o uno por backend y frontend? (Sprint 0)
3. **Aprobación en nombre del cliente:** ¿puede el técnico registrar la aprobación si el cliente la da por llamada o en persona? (Sprint 6)
4. **Verificación al aprobar:** ¿basta con el código o se pide además los últimos dígitos del celular? (Sprint 6)
5. **Contraseña o patrón del equipo:** ¿se registra? Es un dato muy sensible. (Sprint 2)
6. **Cobro por revisión:** si el cliente rechaza la cotización, ¿se cobra un costo de diagnóstico? (Sprint 7)
7. **Documento de identidad:** ¿obligatorio u opcional en el cliente? (Sprint 2)
8. **Métodos de pago:** ¿efectivo, Yape, Plin, transferencia, tarjeta? (Sprint 7)
9. **Casos sin reparación:** equipo sin falla o no reparable, ¿qué estado le corresponde? (Sprint 4)
10. **Roles:** ¿hace falta un rol de recepcionista aparte del técnico? (Sprint 1)
11. **Equipos no recogidos:** ¿cuánto tiempo los custodia el taller y qué ocurre después? (Sprint 11)
12. **Canal para celular:** SMS o WhatsApp. (Sprint 10)
13. **Estrategia multi-taller:** esquema compartido con `taller_id` (propuesto) o esquema por taller. (Sprint 1)
