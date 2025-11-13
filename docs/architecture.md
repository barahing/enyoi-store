# 🧱 Enyoi Store — Arquitectura de Microservicios

> Documento de arquitectura técnica  
> Proyecto distribuido basado en **Spring Boot 3 (WebFlux)** y **arquitectura hexagonal**.  
> La meta es garantizar desacoplamiento, escalabilidad y mantenibilidad.

---

## 🧩 1. Visión General

El ecosistema **Enyoi Store** está conformado por varios microservicios independientes que colaboran entre sí mediante **mensajería (RabbitMQ)** y **REST APIs reactivas (WebFlux)**.

Cada microservicio implementa una estructura **hexagonal (Ports & Adapters)**:

┌──────────────────────────────────────────────┐
│ Application Layer                            │
│ - Services (UseCases)                        │
│ - DTOs / Commands                            │
├──────────────────────────────────────────────┤
│ Domain Layer                                 │
│ - Entities / Aggregates                      │
│ - Domain Events                              │
├──────────────────────────────────────────────┤
│ Infrastructure Layer                         │
│ - REST Controllers (Inbound Adapters)        │
│ - JPA / Reactive Repositories (Outbound)     │
│ - RabbitMQ / WebClient Adapters              │
└──────────────────────────────────────────────┘

**Comunicación entre servicios:**
- 📨 **RabbitMQ** para eventos asincrónicos.  
- 🌐 **REST APIs (WebFlux)** para comunicación directa.  
- 🔒 **JWT (Auth Service)** para autenticación centralizada.

---

## ⚙️ 2. Microservicios Principales

| Microservicio | Responsabilidad | Puerto | Base de datos | Dependencias clave |
|----------------|----------------|--------|----------------|--------------------|
| 🛒 **Carts** | Gestión de carritos de compra | 8081 | PostgreSQL | `store-common`, `products`, `auth-service` |
| 📦 **Orders** | Confirmación y seguimiento de órdenes | 8082 | PostgreSQL | `carts`, `payments`, `stock` |
| 💳 **Payments** | Procesamiento de pagos | 8083 | PostgreSQL | `orders`, `notifications` |
| 🧾 **Products** | Catálogo de productos y categorías | 8084 | PostgreSQL | `stock` |
| 📉 **Stock** | Gestión de inventario y reservas | 8085 | PostgreSQL | `orders`, `products` |
| 👤 **Users** | Registro y autenticación de usuarios | 8086 | PostgreSQL | `auth-service` |
| ✉️ **Notifications** | Envío de correos y reportes PDF | 8088 | SMTP, Filesystem | `orders`, `store-common` |
| 🔐 **Auth Service** | Generación y validación de tokens JWT | 8089 | H2 (mock) | `store-common` |
| ⚙️ **Store Common** | Librería compartida: configs, DTOs, security | — | — | — |

---

## 🔁 3. Flujo de Comunicación Principal

### 🛒 Proceso de Compra (Flujo principal)

[Cliente] → [Carts] → [Orders] → [Payments] → [Stock] → [Notifications]
Secuencia resumida:

Se crea un cliente y automáticamente se crea un carrito y se le asigna.

El cliente agrega productos al carrito (Carts).

Al confirmar, se crea una orden (Orders).

Orders emite un evento → Payments procesa el pago.

Payments emite evento → Stock ajusta inventario.

Orders actualiza estado y notifica a Notifications.

Notifications envía correo o reporte de confirmación.

🔒 4. Seguridad y Autenticación
Arquitectura de Auth Centralizado

                  +---------------------+
                  |     Auth Service    |
                  |  (JWT Generator)    |
                  +---------+-----------+
                            |
                            v
+------------+     +------------+     +------------+
|  Carts     |     |  Orders    |     |  Products  |
| Validates  | --> | via JWT    | --> | via Common |
+------------+     +------------+     +------------+
Características:

JWT firmado con HS256 y clave central (compartida vía store-common).

Cada microservicio incluye SecurityConfig condicional.

store-common provee configuración base:

/api/public/**, /swagger-ui/** → acceso libre.

Demás endpoints → requieren token válido (Futura implementación)

📨 5. Mensajería (RabbitMQ)
Exchanges definidos en store-common/config/RabbitMQConfigCommon.java:
Exchange	Tipo	Descripción
cart.exchange	topic	Eventos de carrito (cart.created, cart.updated)
order.exchange	topic	Eventos de orden (order.created, order.completed)
product.exchange	topic	Sincronización de catálogo y stock
user.exchange	topic	Alta y cambios de usuarios
notification.exchange	topic	Disparos de correo y alertas

Cada microservicio define su binding específico (en su infrastructure/config).

🧩 6. Integraciones y dependencias cruzadas
Fuente	Destino	Tipo	Evento / Endpoint
Carts	Orders	REST + Rabbit	/api/orders/create, cart.checked_out
Orders	Payments	Rabbit	order.created
Payments	Orders	Rabbit	payment.completed
Orders	Stock	Rabbit	order.completed
Orders	Notifications	REST	/api/notifications/sales-report
Auth	Todos	REST	/api/public/auth/login (JWT issuance)

🧠 7. Tecnologías y Librerías
Categoría	Librerías / Frameworks
Core	Spring Boot 3.5.6, Java 17
Reactive	Spring WebFlux
Persistencia	Spring Data R2DBC / PostgreSQL
Mensajería	RabbitMQ
Seguridad	Spring Security WebFlux + JWT
Documentación	SpringDoc OpenAPI 2.7.0
Pruebas	JUnit 5, Testcontainers
Generación de PDF	iText (Lowagie)
Comunicación interservicio	WebClient
Configuración común	store-common (versión 3.1.x)

🧰 8. Arquitectura Hexagonal en Ejemplo (Orders)

orders-microservice
│
├── domain/
│   ├── model/Order.java
│   ├── events/OrderCreated.java
│   └── ports/out/IOrderReportPersistencePort.java
│
├── application/
│   ├── service/WeeklySalesReportService.java
│   └── usecase/CreateOrderUseCase.java
│
└── infrastructure/
    ├── web/controller/WeeklySalesReportEmailController.java
    ├── persistence/repository/OrderRepository.java
    ├── adapter/messaging/OrderEventPublisher.java
    └── config/SecurityConfig.java

🔍 9. Observabilidad y Logs
Todos los servicios usan @Slf4j (Lombok) para logs estructurados.

Cada evento importante genera trazas:

🟢 ✅ Order created: {id}

🔴 ❌ Payment failed: {reason}

Pruebas de integración usan Testcontainers para reproducir entornos reales.

📊 10. Diagrama General Simplificado


                           ┌──────────────────────┐
                           │   Auth Service (JWT) │
                           └──────────┬───────────┘
                                      │
                   ┌──────────────────┴──────────────────┐
                   │                                     │
         ┌─────────▼──────────┐               ┌──────────▼──────────┐
         │   Users Service    │               │   Carts Service     │
         └─────────┬──────────┘               └──────────┬──────────┘
                   │                                     │
         ┌─────────▼──────────┐               ┌──────────▼──────────┐
         │  Orders Service    │──────────────▶│  Payments Service   │
         └─────────┬──────────┘               └──────────┬──────────┘
                   │                                     │
                   ▼                                     ▼
         ┌──────────────────────┐             ┌──────────────────────┐
         │   Stock Service      │             │ Notifications Service│
         └──────────────────────┘             └──────────────────────┘

📘 11. Conclusión
La arquitectura de Enyoi Store permite:

✅ Escalabilidad horizontal por dominio
✅ Desacoplamiento mediante eventos
✅ Seguridad unificada con Auth + store-common
✅ Documentación y pruebas reproducibles
✅ Alta mantenibilidad y trazabilidad funcional

Diseñada siguiendo principios de DDD, Clean Architecture y event-driven design,
con una base sólida para escalar funcional y técnicamente.