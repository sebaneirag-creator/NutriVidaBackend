package com.nutrivida.servicios.repository;

import com.nutrivida.servicios.model.Servicio;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    boolean existsByCategoriaId(Long idCategoria);

    @Query("""
            select s from Servicio s
            where (:idCategoria is null or s.categoria.id = :idCategoria)
              and (:q is null or lower(s.nombre) like lower(concat('%', :q, '%')))
            order by s.id
            """)
    List<Servicio> buscar(@Param("idCategoria") Long idCategoria, @Param("q") String q);

    /** Bloquea la fila mientras dura la transaccion: evita vender mas stock del disponible. */
    @Query(value = "select * from servicio where id = :id for update", nativeQuery = true)
    Optional<Servicio> findByIdForUpdate(@Param("id") Long id);
}
