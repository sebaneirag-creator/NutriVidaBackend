package com.nutrivida.servicios.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "servicio")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 5, unique = true)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String duracion;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private Modalidad modalidad;

    @Column(nullable = false)
    private int precio;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;
}
