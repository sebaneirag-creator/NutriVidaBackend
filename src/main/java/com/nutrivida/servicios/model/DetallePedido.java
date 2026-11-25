package com.nutrivida.servicios.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "detalle_pedido")
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;

    @Column(nullable = false)
    private int cantidad;

    // Precio al momento de la compra (el historial no cambia si el servicio sube de precio)
    @Column(name = "precio_unitario", nullable = false)
    private int precioUnitario;
}
