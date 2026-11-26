package com.nutrivida.servicios.repository;

import com.nutrivida.servicios.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshToken r set r.revocado = true where r.usuario.id = :idUsuario")
    void revocarTodosDelUsuario(@Param("idUsuario") Long idUsuario);

    @Modifying
    @Query("delete from RefreshToken r where r.usuario.id = :idUsuario")
    void eliminarTodosDelUsuario(@Param("idUsuario") Long idUsuario);
}
