# Promaco SpA — API REST (Spring Boot) · Evaluación Parcial 3

Backend de la tienda Promaco SpA: **API REST con Spring Boot 3.5**, base de datos **PostgreSQL** (en AWS EC2), documentación **Swagger** y autenticación **JWT con roles** `ADMIN` y `CLIENTE`.

| | |
|---|---|
| API | `http://localhost:8080/api` (o `http://IP_PUBLICA_EC2:8080/api`) |
| Swagger | `http://localhost:8080/swagger-ui/index.html` |
| OpenAPI | `http://localhost:8080/v3/api-docs` |

## Tecnologías

Java 17 · Spring Boot 3.5.6 (Web, Data JPA, Validation, Security) · PostgreSQL · H2 (respaldo y pruebas) · jjwt 0.12.6 · springdoc-openapi 2.8.13 · JUnit 5 + Mockito + MockMvc.

## Estructura (capas)

```
src/main/java/cl/duoc/promaco/
├── PromacoBackendApplication.java
├── model/        # Entidades JPA: Categoria, Producto, Usuario, Orden, DetalleOrden
│                 #   + ClienteOrden y DireccionEntrega (@Embeddable)
├── repository/   # Interfaces JpaRepository (CRUD + consultas derivadas y JPQL)
├── service/      # Lógica de negocio: descuento 15%, stock, BCrypt, reglas
├── controller/   # Endpoints REST: Auth, Productos, Categorías, Usuarios, Órdenes, Reportes
├── dto/          # LoginRequest, RegistroRequest, AuthResponse, CompraRequest
├── security/     # JwtService, JwtAuthenticationFilter, SecurityConfig, UsuarioDetailsService
├── config/       # OpenApiConfig (Swagger + botón Authorize), DatosIniciales (datos de ejemplo)
└── exception/    # Errores 404/409/400/401/403 en JSON
```

## Modelo de datos

```
categorias 1 ──── N productos
usuarios   1 ──── N ordenes 1 ──── N detalle_orden
```

| Tabla | Campos principales |
|---|---|
| `categorias` | id, nombre (único), descripcion, icono |
| `productos` | id, codigo (único), nombre, descripcion, precio, precio_oferta, stock, stock_critico, imagen, categoria_id (FK) |
| `usuarios` | id, nombre, apellidos, rut, correo (único), clave (**BCrypt**), rol (`admin`/`cliente`), region, comuna, direccion |
| `ordenes` | id, numero, fecha, estado (`pagada`/`rechazada`), motivo, usuario_id (FK), datos del cliente y dirección, subtotal, descuento, total |
| `detalle_orden` | id, orden_id (FK), producto_id, nombre, precio, cantidad |

Las tablas las crea Hibernate (`ddl-auto=update`). Al arrancar con la base vacía, `DatosIniciales` carga 5 categorías, 12 productos, 4 usuarios y 3 órdenes.

## Reglas de negocio

1. **Promoción Constructor:** 15% de descuento en cada línea con **50 o más unidades**.
2. **Sin stock no se vende:** si falta stock, la orden se guarda como `rechazada` y el stock no cambia.
3. Si la compra es exitosa, se **descuenta el stock** de cada producto (`@Transactional`).
4. El backend calcula los precios con los datos de la base: el frontend solo envía `productoId` y `cantidad`.
5. El precio de oferta debe ser menor al precio normal; el código de producto y el correo no se repiten.
6. No se puede borrar una categoría con productos ni la propia cuenta.

## Endpoints

| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/auth/login` · `/api/auth/registro` | Público |
| GET | `/api/auth/me` · POST `/api/auth/renovar` | Con token |
| GET | `/api/productos` · `/api/productos?categoriaId=1` · `/api/productos/ofertas` · `/api/productos/{id}` | Público |
| GET | `/api/productos/criticos` | ADMIN |
| POST · PUT · DELETE | `/api/productos` · `/api/productos/{id}` | ADMIN |
| GET | `/api/categorias` · `/api/categorias/{id}` | Público |
| POST · PUT · DELETE | `/api/categorias` · `/api/categorias/{id}` | ADMIN |
| POST | `/api/ordenes` (comprar) | CLIENTE o ADMIN |
| GET | `/api/ordenes/mias` | CLIENTE o ADMIN |
| GET | `/api/ordenes` · `/api/ordenes/{id}` | ADMIN |
| GET · POST · PUT · DELETE | `/api/usuarios` · `/api/usuarios/{id}` · GET `/api/usuarios/{id}/ordenes` | ADMIN |
| GET | `/api/reportes/ventas` | ADMIN |

Sin token en una ruta protegida → **401**. Con token pero sin el rol → **403**.

## Seguridad (JWT)

1. `POST /api/auth/login` con `{ "correo", "clave" }`. Spring Security compara la clave con el hash **BCrypt** guardado.
2. Si es correcta, `JwtService` genera un token **firmado con HMAC-SHA256** que contiene el correo, los roles y la expiración (**2 horas**).
3. El frontend envía el token en cada petición: `Authorization: Bearer <token>`.
4. `JwtAuthenticationFilter` verifica la firma y la expiración en **cada** petición; si es válido, carga el usuario y sus roles.
5. `SecurityConfig` decide qué rol necesita cada ruta. La API es **stateless**: no guarda sesiones en el servidor.
6. `POST /api/auth/renovar` entrega un token nuevo a partir de uno vigente.

### Probar en Swagger

1. Abre Swagger y ejecuta `POST /api/auth/login` con `{"correo":"mathias@duoc.cl","clave":"admin123"}`.
2. Copia el valor de `token`, presiona **Authorize** y pégalo (sin escribir "Bearer").
3. Prueba los endpoints. Con el usuario `cliente@gmail.com` / `cliente1`, crear un producto responde **403**.

---

## Ejecutar en tu PC

Requisitos: **JDK 17**.

```bash
# Opción A: con PostgreSQL de la EC2 (cambia la IP)
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.url=jdbc:postgresql://IP_PUBLICA_EC2:5432/promaco_db"

# Opción B (respaldo, sin internet): base H2 guardada en la carpeta ./data
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

En Windows usa `mvnw.cmd` en vez de `./mvnw`.

Pruebas: `./mvnw test` (13 pruebas: 5 unitarias de la lógica de compra con Mockito y 8 de integración de seguridad con MockMvc y H2).

---

## Despliegue en AWS EC2 (según la guía del curso)

### 1. Crear la instancia

EC2 → **Launch instance**: nombre `promaco`, **Amazon Linux 2023**, `t3.micro`, par de claves `vockey`.
En el **Security Group** agrega reglas de entrada: **SSH 22**, **TCP 8080** (API) y **HTTP 80** (frontend, opcional), con origen `0.0.0.0/0`.

Conéctate con **Connect → EC2 Instance Connect**.

### 2. Instalar Java 17, Git y PostgreSQL

```bash
sudo dnf update -y
sudo dnf install -y git java-17-amazon-corretto-devel postgresql15 postgresql15-server
sudo postgresql-setup --initdb
sudo sed -i 's/ident/scram-sha-256/g' /var/lib/pgsql/data/pg_hba.conf
sudo systemctl enable --now postgresql
cd /tmp
sudo -u postgres psql -c "CREATE USER promaco_user WITH PASSWORD 'Promaco2026';"
sudo -u postgres psql -c "CREATE DATABASE promaco_db OWNER promaco_user;"
cd ~
```

### 3. Descargar, compilar y ejecutar la API

```bash
git clone https://github.com/USUARIO/REPOSITORIO-BACKEND.git
cd REPOSITORIO-BACKEND
chmod +x mvnw
./mvnw clean package -DskipTests
export DB_URL=jdbc:postgresql://localhost:5432/promaco_db
nohup java -jar target/promaco-backend-1.0.0.jar > api.log 2>&1 &
tail -f api.log          # espera "Started PromacoBackendApplication" y sal con Ctrl + C
```

`nohup ... &` deja la API corriendo aunque cierres la terminal. Para detenerla: `pkill -f promaco-backend`.

### 4. Probar

```bash
curl http://localhost:8080/api/categorias
```

Desde tu PC: `http://IP_PUBLICA_EC2:8080/swagger-ui/index.html`

En el frontend, el archivo `.env` queda así: `VITE_API_URL=http://IP_PUBLICA_EC2:8080/api`

> **Ojo con AWS Academy:** al reiniciar el laboratorio la **IP pública cambia**. Actualiza el `.env` del frontend, o asocia una **Elastic IP** a la instancia para que no cambie.
> Si publicas el frontend con Nginx en la misma EC2, agrega su origen a CORS antes de iniciar la API:
> `export CORS_ORIGINS=http://localhost:5173,http://IP_PUBLICA_EC2`

## Equipo

Mathias Soto · Moise Fonseca · Camilo Godoy
