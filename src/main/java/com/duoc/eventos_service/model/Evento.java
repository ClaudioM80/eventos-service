package com.duoc.eventos_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor  // <-- Agrega esto (constructor vacío)
@AllArgsConstructor // <-- Agrega esto (constructor con todos los campos)
@Table(name = "eventos")
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String fecha; 
    private Double precio;
}