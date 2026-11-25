package com.nutrivida.servicios.repository;

import com.nutrivida.servicios.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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
}
