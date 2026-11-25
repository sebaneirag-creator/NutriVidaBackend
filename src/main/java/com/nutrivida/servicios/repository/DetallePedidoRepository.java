package com.nutrivida.servicios.repository;

import com.nutrivida.servicios.model.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    boolean existsByServicioId(Long idServicio);
}
