package com.nutrivida.servicios.repository;

import com.nutrivida.servicios.model.DetallePedido;
import com.nutrivida.servicios.model.EstadoPedido;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    boolean existsByServicioId(Long idServicio);

    @Query("""
            select coalesce(sum(d.cantidad), 0L) from DetallePedido d
            where d.pedido.fecha >= :desde and d.pedido.estado <> :excluido
            """)
    Long sumarCantidad(@Param("desde") LocalDateTime desde, @Param("excluido") EstadoPedido excluido);

    /** Filas: [nombre, cantidad vendida, ingresos]. */
    @Query("""
            select d.servicio.nombre, sum(d.cantidad), sum(d.cantidad * d.precioUnitario)
            from DetallePedido d
            where d.pedido.fecha >= :desde and d.pedido.estado <> :excluido
            group by d.servicio.id, d.servicio.nombre
            order by sum(d.cantidad) desc, d.servicio.nombre
            """)
    List<Object[]> masSolicitados(@Param("desde") LocalDateTime desde,
                                  @Param("excluido") EstadoPedido excluido,
                                  Pageable pageable);
}
