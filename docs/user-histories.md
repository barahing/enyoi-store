# 🧩 Enyoi Store — Historias de Usuario

> Documento funcional estructurado por **épicas** (microservicios)  
> Proyecto distribuido con arquitectura hexagonal y DDD.  
> Cada historia refleja una necesidad funcional real y su correspondencia técnica  
> dentro del ecosistema de microservicios.

---

## 🏪 Épica 1: Gestión de Carritos (Carts Microservice)

### Historia 1.1 — Agregar producto al carrito
**Como** cliente  
**quiero** agregar un producto a mi carrito  
**para** poder iniciar una compra más adelante.  

**Criterios de aceptación:**
- Se debe crear un carrito nuevo si no existe para el usuario.  
- El servicio debe validar stock antes de agregar el producto.  
- La respuesta incluye el carrito actualizado (lista de items, totales).  

**Microservicios involucrados:**
- `carts-microservice`
- `products-microservice` (consulta stock)
- `store-common` (DTOs, mensajes de evento)

---

### Historia 1.2 — Eliminar producto del carrito
**Como** cliente  
**quiero** eliminar un producto del carrito  
**para** ajustar mi compra antes de pagar.

**Criterios de aceptación:**
- Si el producto no existe en el carrito, devolver 404.  
- El total del carrito se recalcula automáticamente.  

**Microservicios involucrados:**
- `carts-microservice`

---

### Historia 1.3 — Vaciar el carrito
**Como** cliente  
**quiero** vaciar mi carrito completamente  
**para** cancelar mi compra antes del pago.  

**Criterios de aceptación:**
- Se eliminan todos los items asociados al carrito del usuario.  
- Se emite evento `cart.cleared` a RabbitMQ.  

---

## 🧾 Épica 2: Órdenes de Compra (Orders Microservice)

### Historia 2.1 — Confirmar una orden
**Como** cliente  
**quiero** confirmar la compra de mi carrito  
**para** generar una orden registrada en el sistema.  

**Criterios de aceptación:**
- Solo puede confirmarse un carrito con estado `READY`.  
- Se crea una orden con detalle de productos y totales.  
- Se emite evento `order.created`.  

**Microservicios involucrados:**
- `orders-microservice`
- `carts-microservice`
- `stock-microservice`
- `payments-microservice`

---

### Historia 2.2 — Consultar estado de orden
**Como** cliente  
**quiero** consultar el estado actual de mi orden  
**para** conocer si fue pagada, enviada o cancelada.  

**Criterios de aceptación:**
- Devuelve estados `CREATED`, `PAID`, `SHIPPED`, `CANCELLED`.  
- Si la orden no existe, devolver 404.  

---

## 💳 Épica 3: Pagos (Payments Microservice)

### Historia 3.1 — Registrar pago exitoso
**Como** cliente  
**quiero** registrar el pago de una orden  
**para** completar la transacción y recibir mi compra.  

**Criterios de aceptación:**
- Se recibe evento `order.created`.  
- El pago debe cambiar el estado de la orden a `PAID`.  
- Se emite evento `payment.completed`.  

---

### Historia 3.2 — Manejar pago fallido
**Como** cliente  
**quiero** recibir una notificación si mi pago falla  
**para** poder intentar nuevamente.  

**Criterios de aceptación:**
- Se registra el intento fallido con causa.  
- No se cambia el estado de la orden.  
- Se notifica al microservicio de notificaciones.  

---

## 📦 Épica 4: Productos y Stock (Products + Stock Microservices)

### Historia 4.1 — Consultar catálogo de productos
**Como** cliente  
**quiero** ver el listado de productos disponibles  
**para** seleccionar lo que deseo comprar.  

**Criterios de aceptación:**
- Debe incluir nombre, precio, categoría y stock disponible.  
- El catálogo puede filtrarse por categoría.  

**Microservicios involucrados:**
- `products-microservice`
- `stock-microservice`

---

### Historia 4.2 — Actualizar inventario tras una venta
**Como** sistema  
**quiero** disminuir el stock de los productos vendidos  
**para** mantener actualizado el inventario.  

**Criterios de aceptación:**
- Se procesa evento `order.completed`.  
- Si no hay stock suficiente, se genera evento de alerta.  

---

## 👤 Épica 5: Gestión de Usuarios (Users Microservice)

### Historia 5.1 — Registrar nuevo usuario
**Como** visitante  
**quiero** registrarme con mis datos personales  
**para** poder iniciar sesión y realizar compras.  

**Criterios de aceptación:**
- El correo debe ser único.  
- La contraseña se almacena con hash seguro (BCrypt).  
- Devuelve datos básicos y token JWT opcional.  

---

### Historia 5.2 — Autenticación centralizada
**Como** usuario registrado  
**quiero** autenticarme a través del microservicio Auth  
**para** obtener un token JWT válido en todo el ecosistema.  

**Criterios de aceptación:**
- `/auth-service` genera el token.  
- Los demás microservicios (como `carts`) validan el token en requests.  

---

## ✉️ Épica 6: Notificaciones (Notifications Microservice)

### Historia 6.1 — Enviar reporte de ventas semanal
**Como** administrador  
**quiero** recibir por correo el reporte de ventas semanal  
**para** analizar el rendimiento de la tienda.  

**Criterios de aceptación:**
- Genera PDF con totales, productos más vendidos y top clientes.  
- Se envía automáticamente al correo configurado.  
- Puede ser ejecutado manualmente vía endpoint `/send-email`.  

**Microservicios involucrados:**
- `orders-microservice`
- `notifications-microservice`
- `store-common` (eventos compartidos, configuración de RabbitMQ)

---

## 🧠 Épica 7: Seguridad y Autenticación (Auth Service + Common)

### Historia 7.1 — Generar token JWT
**Como** usuario autenticado  
**quiero** recibir un token JWT  
**para** acceder a los microservicios protegidos.  

**Criterios de aceptación:**
- Se valida el usuario en `auth-service`.  
- El token incluye claims `role` y `scope`.  

---

### Historia 7.2 — Validar token en microservicios
**Como** sistema  
**quiero** validar tokens JWT recibidos  
**para** asegurar que solo usuarios autenticados acceden a los endpoints protegidos.  

**Criterios de aceptación:**
- Configuración compartida desde `store-common`.  
- Cada microservicio puede sobrescribir su `SecurityConfig` si requiere permisos distintos.  

---

## 🔗 Trazabilidad General

| Épica | Microservicios | Eventos / Interacciones |
|-------|----------------|--------------------------|
| Carts | `carts`, `products`, `store-common` | `cart.created`, `cart.updated` |
| Orders | `orders`, `carts`, `payments`, `stock` | `order.created`, `order.completed` |
| Payments | `payments`, `orders` | `payment.completed`, `payment.failed` |
| Stock | `stock`, `orders`, `products` | `stock.updated`, `stock.alert` |
| Users | `users`, `auth-service` | `user.created`, `user.logged_in` |
| Notifications | `notifications`, `orders` | `sales-report.generated`, `email.sent` |
| Auth | `auth-service`, `store-common` | JWT validation via `ReactiveSecurity` |

---

## 📚 Notas Técnicas

- Todas las historias se implementan siguiendo arquitectura **hexagonal**: `domain`, `application`, `infrastructure`.
- Comunicación entre microservicios vía **RabbitMQ** y **REST WebFlux**.
- Seguridad unificada con `store-common` + `auth-service`.
- Pruebas de integración planificadas con **Testcontainers (PostgreSQL, RabbitMQ)**.
- Versionado de artefactos local vía `mavenLocal()` con prefijos `3.x.x`.

---

> ✍️ Documento elaborado para presentación técnica ante panel.  
> Cada historia refleja una funcionalidad real dentro del ecosistema **Enyoi Store**.
