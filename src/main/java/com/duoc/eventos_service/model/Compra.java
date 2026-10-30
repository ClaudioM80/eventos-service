package com.duoc.eventos_service.model;

import jakarta.persistence.*;

@Entity
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

    // Constructor vacío
    public Compra() {}

    // Getters y Setters manuales
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Evento getEvento() { return evento; }
    public void setEvento(Evento evento) { this.evento = evento; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }

    public Double getPrecioTotal() { return precioTotal; }
    public void setPrecioTotal(Double precioTotal) { this.precioTotal = precioTotal; }
}