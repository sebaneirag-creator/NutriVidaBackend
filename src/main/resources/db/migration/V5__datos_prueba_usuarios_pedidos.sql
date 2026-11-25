-- Datos de prueba: usuarios (contrasenas con BCrypt) y pedidos de ejemplo
INSERT INTO usuario (nombre, apellido, correo, contrasena, fecha_nacimiento, genero, region, direccion, activo, id_rol) VALUES
 ('Administrador', 'NutriVida', 'admin@nutrivida.cl', '$2b$10$ILonUnvajYS7m96D6ARTT.LNPGw3U0vahXGp6EfpkxeDXN6PgO5Z6', '1985-03-12', 'Femenino', 'Metropolitana', 'Av. Providencia 1234, Providencia', TRUE, (SELECT id FROM rol WHERE nombre = 'ADMINISTRADOR')),
 ('Valentina', 'Rojas', 'vendedor@nutrivida.cl', '$2b$10$xeXINHCMzbr0laQhw/tSxOKg2X2ooz9.dkKAeorVo/ETE8VQJDrkG', '1992-07-25', 'Femenino', 'Metropolitana', 'Los Leones 456, Providencia', TRUE, (SELECT id FROM rol WHERE nombre = 'VENDEDOR')),
 ('Carlos', 'Soto', 'cliente@nutrivida.cl', '$2b$10$oF2jOAJEgWu532GnFwD17..JNu5bPMHNOrIvsb7vmPZ3QBpRg5pF2', '1990-11-02', 'Masculino', 'Metropolitana', 'Irarrazaval 789, Nunoa', TRUE, (SELECT id FROM rol WHERE nombre = 'CLIENTE')),
 ('Maria', 'Gonzalez', 'maria@nutrivida.cl', '$2b$10$pWLB9MgGYtUzvppHQnzwD.7COl7CG43VU3MBWX2BlxIzFhUFy1LQq', '1995-05-18', 'Femenino', 'Valparaiso', 'Av. Libertad 321, Vina del Mar', TRUE, (SELECT id FROM rol WHERE nombre = 'CLIENTE'));

INSERT INTO pedido (id, id_usuario, fecha, estado, total) VALUES
 (1, (SELECT id FROM usuario WHERE correo = 'cliente@nutrivida.cl'), DATE_SUB(NOW(6), INTERVAL 1 DAY), 'CONFIRMADO', 53000),
 (2, (SELECT id FROM usuario WHERE correo = 'maria@nutrivida.cl'), DATE_SUB(NOW(6), INTERVAL 2 DAY), 'PENDIENTE', 30000),
 (3, (SELECT id FROM usuario WHERE correo = 'cliente@nutrivida.cl'), DATE_SUB(NOW(6), INTERVAL 4 DAY), 'COMPLETADO', 25000),
 (4, (SELECT id FROM usuario WHERE correo = 'maria@nutrivida.cl'), DATE_SUB(NOW(6), INTERVAL 6 DAY), 'COMPLETADO', 65000),
 (5, (SELECT id FROM usuario WHERE correo = 'cliente@nutrivida.cl'), DATE_SUB(NOW(6), INTERVAL 9 DAY), 'COMPLETADO', 52000),
 (6, (SELECT id FROM usuario WHERE correo = 'maria@nutrivida.cl'), DATE_SUB(NOW(6), INTERVAL 12 DAY), 'CANCELADO', 70000),
 (7, (SELECT id FROM usuario WHERE correo = 'cliente@nutrivida.cl'), DATE_SUB(NOW(6), INTERVAL 16 DAY), 'COMPLETADO', 170000),
 (8, (SELECT id FROM usuario WHERE correo = 'maria@nutrivida.cl'), DATE_SUB(NOW(6), INTERVAL 21 DAY), 'COMPLETADO', 50000),
 (9, (SELECT id FROM usuario WHERE correo = 'cliente@nutrivida.cl'), DATE_SUB(NOW(6), INTERVAL 27 DAY), 'COMPLETADO', 38000),
 (10, (SELECT id FROM usuario WHERE correo = 'maria@nutrivida.cl'), DATE_SUB(NOW(6), INTERVAL 40 DAY), 'COMPLETADO', 44000),
 (11, (SELECT id FROM usuario WHERE correo = 'cliente@nutrivida.cl'), DATE_SUB(NOW(6), INTERVAL 58 DAY), 'COMPLETADO', 75000),
 (12, (SELECT id FROM usuario WHERE correo = 'maria@nutrivida.cl'), DATE_SUB(NOW(6), INTERVAL 85 DAY), 'COMPLETADO', 45000),
 (13, (SELECT id FROM usuario WHERE correo = 'cliente@nutrivida.cl'), DATE_SUB(NOW(6), INTERVAL 130 DAY), 'COMPLETADO', 68000),
 (14, (SELECT id FROM usuario WHERE correo = 'maria@nutrivida.cl'), DATE_SUB(NOW(6), INTERVAL 240 DAY), 'COMPLETADO', 93000);

INSERT INTO detalle_pedido (id_pedido, id_servicio, cantidad, precio_unitario) VALUES
 (1, (SELECT id FROM servicio WHERE codigo = 'CN001'), 1, 35000),
 (1, (SELECT id FROM servicio WHERE codigo = 'EV001'), 1, 18000),
 (2, (SELECT id FROM servicio WHERE codigo = 'TG001'), 2, 15000),
 (3, (SELECT id FROM servicio WHERE codigo = 'CN002'), 1, 25000),
 (4, (SELECT id FROM servicio WHERE codigo = 'PL001'), 1, 65000),
 (5, (SELECT id FROM servicio WHERE codigo = 'CN004'), 2, 20000),
 (5, (SELECT id FROM servicio WHERE codigo = 'EV002'), 1, 12000),
 (6, (SELECT id FROM servicio WHERE codigo = 'PL003'), 1, 70000),
 (7, (SELECT id FROM servicio WHERE codigo = 'PL002'), 1, 170000),
 (8, (SELECT id FROM servicio WHERE codigo = 'CN001'), 1, 35000),
 (8, (SELECT id FROM servicio WHERE codigo = 'EV004'), 1, 15000),
 (9, (SELECT id FROM servicio WHERE codigo = 'TG002'), 1, 20000),
 (9, (SELECT id FROM servicio WHERE codigo = 'TG003'), 1, 18000),
 (10, (SELECT id FROM servicio WHERE codigo = 'CN003'), 2, 22000),
 (11, (SELECT id FROM servicio WHERE codigo = 'PL004'), 1, 75000),
 (12, (SELECT id FROM servicio WHERE codigo = 'CN001'), 1, 35000),
 (12, (SELECT id FROM servicio WHERE codigo = 'EV003'), 1, 10000),
 (13, (SELECT id FROM servicio WHERE codigo = 'PL005'), 1, 68000),
 (14, (SELECT id FROM servicio WHERE codigo = 'PL006'), 1, 65000),
 (14, (SELECT id FROM servicio WHERE codigo = 'CN005'), 1, 28000);
