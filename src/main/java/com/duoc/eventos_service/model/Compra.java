package com.duoc.eventos_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "compras")
public class Compra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "evento_id")
    private Evento evento;

    private Integer cantidad;
    private Double precioUnitario;
    private Double precioTotal;
}