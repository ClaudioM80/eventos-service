package com.duoc.eventos_service.controller;

import com.duoc.eventos_service.model.Compra;
import com.duoc.eventos_service.model.Evento;
import com.duoc.eventos_service.repository.CompraRepository;
import com.duoc.eventos_service.repository.EventoRepository;
import com.duoc.eventos_service.service.AwsS3Service;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
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

    @Autowired
    private AwsS3Service awsS3Service; // Inyectamos el servicio de S3

    private final String BUCKET_NAME = "eventos-cm-2026"; // Tu bucket de S3

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
        Compra compra = compraRepository.findById(idCompra)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada con ID: " + idCompra));

        try {
            // 1. GENERAR EL PDF EN MEMORIA
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();
            document.add(new Paragraph("=== TICKET DE COMPRA ==="));
            document.add(new Paragraph("ID Compra: " + compra.getId()));
            document.add(new Paragraph("Evento: " + compra.getEvento().getNombre()));
            document.add(new Paragraph("Cantidad de entradas: " + compra.getCantidad()));
            document.add(new Paragraph("Precio unitario: $" + compra.getPrecioUnitario()));
            document.add(new Paragraph("Total pagado: $" + compra.getPrecioTotal()));
            document.add(new Paragraph("¡Gracias por tu compra!"));
            document.close();
            byte[] pdfBytes = baos.toByteArray();

            // 2. GUARDAR TEMPORALMENTE EN EFS
            String fileName = "ticket_" + idCompra + ".pdf";
            Path efsPath = Paths.get("/app/efs", fileName);
            Files.createDirectories(efsPath.getParent()); 
            Files.write(efsPath, pdfBytes); 

            // 3. SUBIR A AWS S3 (Estructura: evento1/usuario1/ticket_1.pdf)
            String s3Key = "evento" + compra.getEvento().getId() + "/usuario1/" + fileName;
            MultipartFile multipartFile = new MockMultipartFile(fileName, fileName, "application/pdf", pdfBytes);
            awsS3Service.upload(BUCKET_NAME, s3Key, multipartFile);

            // 4. RETORNAR EL PDF PARA DESCARGA
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);

        } catch (Exception e) {
            throw new RuntimeException("Error generando y subiendo el PDF: " + e.getMessage(), e);
        }
    }
}