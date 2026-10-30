package com.duoc.eventos_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "eventos")
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String fecha; 
    private Double precio;

    // Constructor vacío (necesario para Spring)
    public Evento() {}

    // Getters y Setters manuales (adiós errores de Lombok)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
}