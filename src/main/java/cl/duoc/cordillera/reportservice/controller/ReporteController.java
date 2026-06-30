package cl.duoc.cordillera.reportservice.controller;

import cl.duoc.cordillera.reportservice.dto.KpiResumenDto;
import cl.duoc.cordillera.reportservice.model.Reporte;
import cl.duoc.cordillera.reportservice.service.ReporteService;
import cl.duoc.cordillera.reportservice.service.client.KpiClienteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reportes")
public class ReporteController implements ReporteApi {

    private final ReporteService reporteService;
    private final KpiClienteService kpiClienteService;


    @GetMapping
    public ResponseEntity<List<Reporte>> listarTodos() {
        return ResponseEntity.ok(reporteService.listarTodos());
    }

    @Override
    @GetMapping("/area/{area}")
    public ResponseEntity<List<Reporte>> listarPorArea(@PathVariable String area) {
        return ResponseEntity.ok(reporteService.listarPorArea(area));
    }

    @Override
    @GetMapping("/kpis")
    public ResponseEntity<List<KpiResumenDto>> listarKpis() {
        return ResponseEntity.ok(kpiClienteService.obtenerKpis());
    }

    @Override
    @GetMapping("/kpis/categoria/{categoria}")
    public ResponseEntity<List<KpiResumenDto>> listarKpisPorCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(kpiClienteService.obtenerKpisPorCategoria(categoria));
    }

    @Override
    @GetMapping("/{id}/exportar")
    public ResponseEntity<byte[]> exportar(
            @PathVariable Long id,
            @RequestParam(defaultValue = "pdf") String formato) {
        byte[] contenido = reporteService.exportar(id, formato);
        Reporte reporte = reporteService.buscarPorId(id);

        String extension = switch (formato.toLowerCase()) {
            case "excel", "xls", "xlsx" -> "xls";
            case "json" -> "json";
            default -> "pdf";
        };

        String contentType = switch (formato.toLowerCase()) {
            case "excel", "xls", "xlsx" -> "application/vnd.ms-excel";
            case "json" -> "application/json";
            default -> "application/pdf";
        };

        String tituloLimpio = reporte.getTitulo().replaceAll("[^a-zA-Z0-9]+", "_");
        if (tituloLimpio.length() > 30) {
            tituloLimpio = tituloLimpio.substring(0, 30);
        }
        if (tituloLimpio.endsWith("_")) {
            tituloLimpio = tituloLimpio.substring(0, tituloLimpio.length() - 1);
        }
        
        String fecha = reporte.getFechaGeneracion().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        String nombreArchivo = "Cordillera_Reporte_" + tituloLimpio + "_" + fecha + "." + extension;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .body(contenido);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<Reporte> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reporteService.buscarPorId(id));
    }

    @Override
    @PostMapping
    public ResponseEntity<Reporte> crear(@Valid @RequestBody Reporte reporte) {
        // Fix EP3 (cambio 2 del profesor): delega en generarReporte() para que aplique
        // la regla de unicidad (area, tipo, anio, mes). Asi se evita el caso reportado
        // por el profesor: "se generaba 2 veces el mismo reporte de los meses".
        Reporte reporteResultado = reporteService.generarReporte(reporte);
        if (reporteResultado.getId() != null && !reporteResultado.getId().equals(reporte.getId())) {
            // Se devuelve un reporte ya existente -> 200 OK
            return ResponseEntity.ok(reporteResultado);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(reporteResultado);
    }

    @Override
    @PostMapping("/generar")
    public ResponseEntity<Reporte> generar(@RequestBody Reporte reporte) {
        Reporte reporteGenerado = reporteService.generarReporte(reporte);
        return ResponseEntity.status(HttpStatus.CREATED).body(reporteGenerado);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<Reporte> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Reporte reporte) {
        return ResponseEntity.ok(reporteService.actualizar(id, reporte));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        reporteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}