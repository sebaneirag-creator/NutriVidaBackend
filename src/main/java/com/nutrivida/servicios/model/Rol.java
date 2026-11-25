package com.nutrivida.servicios.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ADMINISTRADOR, VENDEDOR o CLIENTE
    @Column(nullable = false, length = 20, unique = true)
    private String nombre;
}
