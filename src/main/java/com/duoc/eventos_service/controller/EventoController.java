package com.duoc.eventos_service.controller;

import com.duoc.eventos_service.model.Compra;
import com.duoc.eventos_service.model.Evento;
import com.duoc.eventos_service.repository.CompraRepository;
import com.duoc.eventos_service.repository.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api")
public class EventoController {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private CompraRepository compraRepository;

    @GetMapping("/eventos")
    public List<Evento> listarEventos() {
        return eventoRepository.findAll();
    }

    @PostMapping("/eventos")
    public ResponseEntity<?> agregarEvento(@RequestBody Evento evento) {
        try {
            Evento guardado = eventoRepository.save(evento);
            return ResponseEntity.ok(guardado);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

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
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    static class CompraRequest {
        private Long eventoId;
        private Integer cantidad;

        public Long getEventoId() { return eventoId; }
        public void setEventoId(Long eventoId) { this.eventoId = eventoId; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    }

    @GetMapping("/resumen/descargar/{idCompra}")
    public ResponseEntity<ByteArrayResource> descargarResumen(@PathVariable Long idCompra) {
        // 1. Buscamos la compra REAL en la base de datos
        Compra compra = compraRepository.findById(idCompra)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada con ID: " + idCompra));

        // 2. Armamos el documento con los datos REALES (incluyendo cantidad y precio unitario)
        String contenido = "=== RESUMEN DE COMPRA ===\n" +
                           "ID Compra: " + compra.getId() + "\n" +
                           "Evento: " + compra.getEvento().getNombre() + "\n" +
                           "Cantidad de entradas: " + compra.getCantidad() + "\n" +
                           "Precio unitario: $" + compra.getPrecioUnitario() + "\n" +
                           "Total pagado: $" + compra.getPrecioTotal() + "\n" +
                           "¡Gracias por tu compra!";
                           
        byte[] data = contenido.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        // 3. Guardamos temporalmente en EFS (Requisito de la evaluación)
        try {
            Path efsPath = Paths.get("/app/efs", "resumen_" + idCompra + ".txt");
            Files.createDirectories(efsPath.getParent()); // Crea la carpeta si no existe
            Files.write(efsPath, data); // Guarda el archivo en EFS
            System.out.println("Archivo guardado exitosamente en EFS: " + efsPath.toString());
        } catch (IOException e) {
            System.err.println("Error guardando en EFS (Si estás en local, ignora esto): " + e.getMessage());
        }

        // 4. Preparamos la descarga
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resumen_" + idCompra + ".txt")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}