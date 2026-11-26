# Pruebas de integración - NutriVida

## Automáticas (`./mvnw test`)
Usan la base `nutrivida_test` (distinta de la de desarrollo). Cada prueba se revierte al terminar.

| Caso | Esperado |
|---|---|
| Login correcto | 200, devuelve accessToken y refreshToken, sin contraseña |
| Login incorrecto | 401 |
| 3 intentos fallidos | 423 (cuenta bloqueada, también con la clave correcta) |
| Refresh | 200 la primera vez; con el token anterior 401 |
| Logout | 204; luego el refresh token da 401 |
| Registro público | 201 y rol CLIENTE aunque se envíe otro |
| Sin token en ruta protegida | 401 |
| Token manipulado / expirado | 401 |
| Cliente en `/api/usuarios` | 403 (admin: 200) |
| Catálogo sin sesión | 200; escribir sin rol 403 |
| CRUD de categorías y servicios | 201, 409 duplicado, 400 validación, 204 |
| Pedido válido | 201, total calculado en servidor, stock descontado |
| Pedido sin stock | 400 |
| Historial | el pedido aparece en `mis-pedidos` |
| Pedido de otro usuario | 404 (admin: 200) |
| Cancelar pedido | 200 y el stock vuelve |
| Cambio de estado | cliente 403; admin 200; transición inválida 400 |
| Reportes | cliente 403; admin 200 con datos; periodo inválido 400; se actualiza al crear un pedido |
| Vendedor cambia stock | 200; borrar o crear servicios 403 |
| Vendedor crea cliente | 201 con rol CLIENTE; `POST /usuarios` 403 |
| Registro con correo ajeno al dominio | 400 |
| Registro con contraseña débil | 400 |
| Registro de menor de 14 años | 400 (con 14 años exactos, 201) |
| Cambio de contraseña con la actual incorrecta | 400 |
| Cuenta desactivada | 401 con clave errónea; 423 con la correcta |

## Manuales (frontend + backend + base de datos)
| # | Caso | Pasos | Esperado | Obtenido | ✔/✘ | Fecha |
|---|------|-------|----------|----------|-----|-------|
| 1 | Persistencia | Cambiar stock, reiniciar backend | El stock se mantiene | El stock se mantiene | ✔| 26/11|
| 2 | Sesión | Iniciar sesión y recargar la página | Sigue autenticado | Sigue autenticado| ✔| 26/11|
| 3 | Expiración | Borrar el token y navegar | Redirige a login | Redirige a login| ✔| 26/11|
| 4 | Historial | Comprar y abrir "Mis pedidos" | Aparece el pedido nuevo | Aparece el pedido| ✔| 26/11|
| 5 | Privacidad | Cliente A pide el pedido de B por URL | 404 | Error 404| ✔| 26/11|
