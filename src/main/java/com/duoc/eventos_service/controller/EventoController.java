package com.duoc.eventos_service.controller;

import com.duoc.eventos_service.model.Compra;
import com.duoc.eventos_service.model.Evento;
import com.duoc.eventos_service.repository.CompraRepository;
import com.duoc.eventos_service.repository.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EventoController {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private CompraRepository compraRepository;

    // Endpoint 1: Listar eventos
    @GetMapping("/eventos")
    public List<Evento> listarEventos() {
        return eventoRepository.findAll();
    }

    // Endpoint 2: Agregar evento
    @PostMapping("/eventos")
    public Evento agregarEvento(@RequestBody Evento evento) {
        return eventoRepository.save(evento);
    }

    // Endpoint 3: Comprar entradas
    @PostMapping("/compras")
    public ResponseEntity<?> comprarEntradas(@RequestBody CompraRequest request) {
        // Buscar el evento
        Evento evento = eventoRepository.findById(request.getEventoId())
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        // Calcular total
        double total = evento.getPrecio() * request.getCantidad();

        // Crear la compra
        Compra compra = new Compra();
        compra.setEvento(evento);
        compra.setCantidad(request.getCantidad());
        compra.setPrecioUnitario(evento.getPrecio());
        compra.setPrecioTotal(total);

        compraRepository.save(compra);

        // Respuesta resumen
        return ResponseEntity.ok(compra);
    }
}

// Clase interna para recibir la petición de compra
class CompraRequest {
    private Long eventoId;
    private Integer cantidad;

    // Getters y Setters
    public Long getEventoId() { return eventoId; }
    public void setEventoId(Long eventoId) { this.eventoId = eventoId; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}