package com.duoc.eventos_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "eventos")
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String fecha; // Puedes usar LocalDate si prefieres
    private Double precio;
}