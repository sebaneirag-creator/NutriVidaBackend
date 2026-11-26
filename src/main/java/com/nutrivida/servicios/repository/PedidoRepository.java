package com.nutrivida.servicios.repository;

import com.nutrivida.servicios.model.EstadoPedido;
import com.nutrivida.servicios.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioIdOrderByFechaDesc(Long idUsuario);

    List<Pedido> findAllByOrderByFechaDesc();

    List<Pedido> findByEstadoOrderByFechaDesc(EstadoPedido estado);

    boolean existsByUsuarioId(Long idUsuario);

    // ----- Reportes (los pedidos CANCELADO no cuentan como venta) -----

    long countByFechaGreaterThanEqualAndEstadoNot(LocalDateTime desde, EstadoPedido estado);

    @Query("select coalesce(sum(p.total), 0L) from Pedido p where p.fecha >= :desde and p.estado <> :excluido")
    Long sumarTotal(@Param("desde") LocalDateTime desde, @Param("excluido") EstadoPedido excluido);

    @Query("select p.estado, count(p) from Pedido p where p.fecha >= :desde group by p.estado")
    List<Object[]> contarPorEstado(@Param("desde") LocalDateTime desde);

    @Query("select p.fecha, p.total from Pedido p where p.fecha >= :desde and p.estado <> :excluido order by p.fecha")
    List<Object[]> fechasYTotales(@Param("desde") LocalDateTime desde, @Param("excluido") EstadoPedido excluido);
}
