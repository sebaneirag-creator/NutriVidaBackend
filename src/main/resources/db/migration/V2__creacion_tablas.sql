CREATE TABLE `categoria` (
                             `id` bigint(20) NOT NULL,
                             `nombre` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `servicio`
--

CREATE TABLE `servicio` (
                            `id` bigint(20) NOT NULL,
                            `codigo` varchar(5) NOT NULL,
                            `descripcion` varchar(255) NOT NULL,
                            `duracion` varchar(20) NOT NULL,
                            `modalidad` enum('ONLINE_VIDEO','PRESENCIAL','PRESENCIAL_GRUPO') NOT NULL,
                            `nombre` varchar(80) NOT NULL,
                            `precio` int(11) NOT NULL,
                            `stock` int(11) NOT NULL,
                            `id_categoria` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `categoria`
--
ALTER TABLE `categoria`
    ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `servicio`
--
ALTER TABLE `servicio`
    ADD PRIMARY KEY (`id`),
  ADD KEY `FK67yut59rqk28sceh5o0yfc4u1` (`id_categoria`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `categoria`
--
ALTER TABLE `categoria`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `servicio`
--
ALTER TABLE `servicio`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `servicio`
--
ALTER TABLE `servicio`
    ADD CONSTRAINT `FK67yut59rqk28sceh5o0yfc4u1` FOREIGN KEY (`id_categoria`) REFERENCES `categoria` (`id`);
COMMIT;