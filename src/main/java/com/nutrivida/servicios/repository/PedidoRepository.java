package com.nutrivida.servicios.repository;

import com.nutrivida.servicios.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioIdOrderByFechaDesc(Long idUsuario);

    boolean existsByUsuarioId(Long idUsuario);
}
