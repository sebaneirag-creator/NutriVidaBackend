-- Restricciones de unicidad
ALTER TABLE categoria ADD CONSTRAINT uk_categoria_nombre UNIQUE (nombre);
ALTER TABLE servicio  ADD CONSTRAINT uk_servicio_codigo  UNIQUE (codigo);

-- Modalidad como VARCHAR (el enum de MySQL da problemas con la validacion de Hibernate)
ALTER TABLE servicio MODIFY modalidad VARCHAR(20) NOT NULL;

CREATE TABLE rol (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(20) NOT NULL,
  CONSTRAINT uk_rol_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO rol (nombre) VALUES ('ADMINISTRADOR'), ('VENDEDOR'), ('CLIENTE');

CREATE TABLE usuario (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(50) NOT NULL,
  apellido VARCHAR(50) NOT NULL,
  correo VARCHAR(100) NOT NULL,
  contrasena VARCHAR(100) NOT NULL,
  fecha_nacimiento DATE NULL,
  genero VARCHAR(20) NULL,
  region VARCHAR(60) NULL,
  direccion VARCHAR(150) NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  intentos_fallidos INT NOT NULL DEFAULT 0,
  bloqueado_hasta DATETIME(6) NULL,
  id_rol BIGINT NOT NULL,
  CONSTRAINT uk_usuario_correo UNIQUE (correo),
  CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE pedido (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  id_usuario BIGINT NOT NULL,
  fecha DATETIME(6) NOT NULL,
  estado VARCHAR(20) NOT NULL,
  total INT NOT NULL,
  CONSTRAINT fk_pedido_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE detalle_pedido (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  id_pedido BIGINT NOT NULL,
  id_servicio BIGINT NOT NULL,
  cantidad INT NOT NULL,
  precio_unitario INT NOT NULL,
  CONSTRAINT fk_detalle_pedido   FOREIGN KEY (id_pedido)   REFERENCES pedido (id),
  CONSTRAINT fk_detalle_servicio FOREIGN KEY (id_servicio) REFERENCES servicio (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE refresh_token (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  id_usuario BIGINT NOT NULL,
  token_hash VARCHAR(64) NOT NULL,
  expira_en DATETIME(6) NOT NULL,
  revocado BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash),
  CONSTRAINT fk_refresh_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
