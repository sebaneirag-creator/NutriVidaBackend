package com.nutrivida.servicios.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String nombre;

    // Sin cascade: borrar una categoria NO debe borrar sus servicios
    @OneToMany(mappedBy = "categoria")
    private List<Servicio> servicios = new ArrayList<>();
}
