# NutriVida - Backend

API REST de la clínica de nutrición **NutriVida**: catálogo de servicios nutricionales, usuarios con roles, autenticación con JWT, pedidos con historial y reportes administrativos.

Proyecto semestral. Este repositorio contiene solo el **backend**; el frontend (React + TypeScript + Vite) está en su propio repositorio.

## Tecnologías

## Requisitos

- Java 21
- MySQL 8 o MariaDB 10.x / 11.x
- Maven (se incluye el wrapper `./mvnw`)

## Puesta en marcha


### 1. Configurar las variables de entorno

Las credenciales y claves **no se guardan en el repositorio**: se leen del entorno.

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `DB_URL` | URL JDBC de la base de datos | `jdbc:mysql://localhost:3306/nutrivida` |
| `DB_USER` | Usuario de la base de datos | `root` |
| `DB_PASSWORD` | Contraseña de la base de datos | *(vacía)* |
| `JWT_SECRET` | Clave de firma de los tokens (**mínimo 32 caracteres**) | *obligatoria, sin valor por defecto* |
| `CORS_ORIGEN` | Origen del frontend permitido | `http://localhost:5173` |

Ejemplo en Linux o macOS:

```bash
export DB_PASSWORD='mi_clave'
export JWT_SECRET='una-clave-larga-y-aleatoria-de-32-caracteres-o-mas'
```

En IntelliJ IDEA: *Run → Edit Configurations → Environment variables*.

En `src/main/resources/application-example.properties` hay un archivo de referencia con los nombres de las variables.

### 2. Ejecutar

```bash
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

### 3. Swagger

Documentación interactiva de todos los endpoints:

**http://localhost:8080/swagger-ui.html**

Para probar endpoints protegidos: hacer `POST /api/auth/login`, copiar el `accessToken`, pulsar **Authorize** y pegarlo (sin la palabra `Bearer`).

## Usuarios de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | `admin@nutrivida.cl` | `Admin123` |
| Vendedor | `vendedor@nutrivida.cl` | `Vendedor123` |
| Cliente | `cliente@nutrivida.cl` | `Cliente123` |
| Cliente | `maria@nutrivida.cl` | `Cliente456` |

Los datos de prueba incluyen 4 categorías, 18 servicios y 14 pedidos repartidos en el último año, para que el historial y el dashboard tengan información desde el primer arranque.

## Roles y permisos

| Rol | Puede hacer |
|---|---|
| **Visitante** (sin sesión) | Ver el catálogo (servicios y categorías), registrarse e iniciar sesión |
| **CLIENTE** | Todo lo anterior, más: crear pedidos, ver y cancelar **sus propios** pedidos, ver y editar su perfil |
| **VENDEDOR** | Todo lo del cliente, más: gestionar servicios, ver todos los pedidos, cambiar su estado y consultar reportes |
| **ADMINISTRADOR** | Todo lo anterior, más: gestionar categorías y usuarios (crear, editar, activar, desactivar, desbloquear, eliminar) |

Los permisos se validan **en el backend**, no solo en el frontend: una solicitud sin token válido recibe `401` y una con rol insuficiente recibe `403`.

## Endpoints

### Autenticación (públicos)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/auth/login` | Inicia sesión. Devuelve access token, refresh token y datos del usuario |
| POST | `/api/auth/registro` | Registro público (siempre crea un `CLIENTE`) |
| POST | `/api/auth/refresh` | Renueva la sesión con el refresh token (se rota en cada uso) |
| POST | `/api/auth/logout` | Revoca el refresh token |

### Servicios

| Método | Ruta | Acceso |
|---|---|---|
| GET | `/api/servicios` (filtros `?categoria=` y `?q=`) | Público |
| GET | `/api/servicios/{id}` | Público |
| POST | `/api/servicios` | Administrador, vendedor |
| PUT | `/api/servicios/{id}` | Administrador, vendedor |
| DELETE | `/api/servicios/{id}` | Administrador, vendedor |

### Categorías

| Método | Ruta | Acceso |
|---|---|---|
| GET | `/api/categorias`, `/api/categorias/{id}` | Público |
| POST, PUT, DELETE | `/api/categorias` y `/api/categorias/{id}` | Administrador |

### Usuarios

| Método | Ruta | Acceso |
|---|---|---|
| GET, PUT | `/api/usuarios/me` | Cualquier usuario autenticado |
| GET | `/api/usuarios`, `/api/usuarios/{id}` | Administrador |
| POST | `/api/usuarios` | Administrador |
| PUT | `/api/usuarios/{id}` | Administrador |
| PATCH | `/api/usuarios/{id}/activo` | Administrador |
| PATCH | `/api/usuarios/{id}/desbloquear` | Administrador |
| DELETE | `/api/usuarios/{id}` | Administrador |

### Pedidos

| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/pedidos` | Usuario autenticado (queda asociado al usuario del token) |
| GET | `/api/pedidos/mis-pedidos` | Usuario autenticado (solo los suyos) |
| GET | `/api/pedidos/{id}` | Dueño del pedido, vendedor o administrador |
| PATCH | `/api/pedidos/{id}/cancelar` | Dueño del pedido, vendedor o administrador |
| GET | `/api/pedidos` (filtro `?estado=`) | Administrador, vendedor |
| PATCH | `/api/pedidos/{id}/estado` | Administrador, vendedor |

### Reportes

| Método | Ruta | Acceso |
|---|---|---|
| GET | `/api/reportes/resumen?periodo=semana\|mes\|anio` | Administrador, vendedor |

## Reglas de negocio destacadas

- **Contraseñas:** se almacenan con hash BCrypt; nunca se devuelven en las respuestas.
- **Bloqueo de cuenta:** tras 3 intentos fallidos de inicio de sesión, la cuenta se bloquea 5 minutos (`423 Locked`). Un administrador puede desbloquearla antes.
- **Tokens:** el access token dura 30 minutos y el refresh token 7 días. El refresh token se rota en cada renovación y en la base de datos solo se guarda su hash.
- **Pedidos:**
  - El total se calcula en el servidor; el precio de cada servicio queda congelado en el detalle del pedido.
  - El stock se valida y descuenta dentro de una transacción con bloqueo de fila, para evitar vender más cupos de los disponibles.
  - Un pedido cancelado devuelve el stock.
  - Estados: `PENDIENTE → CONFIRMADO → COMPLETADO`, o `CANCELADO` desde pendiente o confirmado.
- **Privacidad del historial:** un cliente que consulta el pedido de otro usuario recibe `404`.
- **Integridad:** no se puede eliminar un servicio que ya forma parte de pedidos, una categoría con servicios, ni un usuario con pedidos (en ese caso se desactiva).
- **Reportes:** los pedidos cancelados no cuentan como venta. Los períodos son móviles: últimos 7, 30 o 365 días.

## Formato de errores

Todas las respuestas de error usan el mismo JSON:

```json
{
  "timestamp": "2026-11-25T22:55:33",
  "status": 400,
  "error": "Bad Request",
  "mensaje": "Datos inválidos",
  "errores": ["precio: El precio no puede ser negativo"]
}
```

| Código | Cuándo ocurre |
|---|---|
| 400 | Validación fallida o regla de negocio (por ejemplo, stock insuficiente) |
| 401 | Sin token, token inválido o expirado, credenciales incorrectas |
| 403 | El rol no tiene permiso para la acción |
| 404 | Recurso inexistente (o pedido de otro usuario) |
| 409 | Conflicto: dato duplicado o elemento en uso |
| 423 | Cuenta bloqueada o desactivada |

## Modelo de datos

```
rol 1───* usuario 1───* pedido 1───* detalle_pedido *───1 servicio *───1 categoria
                │
                └───* refresh_token
```

| Tabla | Contenido |
|---|---|
| `rol` | ADMINISTRADOR, VENDEDOR, CLIENTE |
| `usuario` | Datos personales, correo único, contraseña con hash, rol, estado y control de intentos fallidos |
| `categoria` | Categorías de servicios (nombre único) |
| `servicio` | Código único, nombre, duración, modalidad, precio, stock, descripción y categoría |
| `pedido` | Usuario, fecha, estado y total |
| `detalle_pedido` | Servicio, cantidad y precio unitario al momento de la compra |
| `refresh_token` | Hash del token, vencimiento y estado de revocación |

El esquema se versiona con Flyway en `src/main/resources/db/migration`:

| Migración | Contenido |
|---|---|
| `V2` | Tablas de categorías y servicios |
| `V3` | Datos iniciales del catálogo |
| `V4` | Roles, usuarios, pedidos, detalles y refresh tokens |
| `V5` | Usuarios y pedidos de prueba |

## Estructura del proyecto

```
src/main/java/com/nutrivida/servicios
├── config/        Seguridad, CORS y OpenAPI
├── controller/    Endpoints REST
├── dto/           Objetos de petición y respuesta
├── exception/     Excepciones y manejador global de errores
├── model/         Entidades JPA
├── repository/    Repositorios Spring Data
├── security/      Servicio JWT, filtro de autenticación y respuestas 401/403
└── service/       Lógica de negocio
```

## Pruebas

Las pruebas de integración recorren la cadena completa (HTTP → seguridad → servicios → base de datos). Usan una base **separada** para no tocar los datos de desarrollo

Si la base de pruebas no está en `localhost:3306/nutrivida_test`, se puede cambiar con la variable `DB_TEST_URL`. Flyway crea las tablas automáticamente y cada prueba se revierte al terminar.

Cubren inicio de sesión correcto e incorrecto, bloqueo de cuenta, renovación y cierre de sesión, acceso sin token, token manipulado o expirado, permisos por rol, CRUD de categorías y servicios, creación de pedidos con control de stock, historial, aislamiento entre usuarios y reportes.

Los casos probados y la plantilla de pruebas manuales están en [`docs/pruebas-integracion.md`](docs/pruebas-integracion.md).

## Seguridad del repositorio

- Las contraseñas de la base de datos y la clave `JWT_SECRET` se leen del entorno y no se versionan.
- `.gitignore` excluye `.env` y `application-local.properties`.
- La clave que aparece en `src/test/resources/application-test.properties` es exclusiva de las pruebas automáticas.
