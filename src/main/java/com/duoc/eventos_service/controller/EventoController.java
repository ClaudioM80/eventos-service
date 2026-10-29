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

    // Endpoint 2: Agregar evento (CON MODO DEBUG)
    @PostMapping("/eventos")
    public ResponseEntity<?> agregarEvento(@RequestBody Evento evento) {
        try {
            Evento guardado = eventoRepository.save(evento);
            return ResponseEntity.ok(guardado);
        } catch (Exception e) {
            // Esto imprimirá el error real en los logs de AWS
            e.printStackTrace(); 
            // Esto nos devolverá el error real en Postman
            return ResponseEntity.status(500).body("Error: " + e.getMessage() + " | Causa: " + e.getCause());
        }
    }

    // Endpoint 3: Comprar entradas
    @PostMapping("/compras")
    public ResponseEntity<?> comprarEntradas(@RequestBody CompraRequest request) {
        try {
            Evento evento = eventoRepository.findById(request.getEventoId())
                    .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

            double total = evento.getPrecio() * request.getCantidad();

            Compra compra = new Compra();
            compra.setEvento(evento);
            compra.setCantidad(request.getCantidad());
            compra.setPrecioUnitario(evento.getPrecio());
            compra.setPrecioTotal(total);

            compraRepository.save(compra);
            return ResponseEntity.ok(compra);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}

// Clase interna (Ahora es public static)
public static class CompraRequest {
    private Long eventoId;
    private Integer cantidad;

    public Long getEventoId() { return eventoId; }
    public void setEventoId(Long eventoId) { this.eventoId = eventoId; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}