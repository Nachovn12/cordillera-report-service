package cl.duoc.cordillera.reportservice.controller;

import cl.duoc.cordillera.reportservice.dto.KpiResumenDto;
import cl.duoc.cordillera.reportservice.model.Reporte;
import cl.duoc.cordillera.reportservice.service.ReporteService;
import cl.duoc.cordillera.reportservice.service.client.KpiClienteService;
import cl.duoc.cordillera.reportservice.service.exportador.Exportador;
import cl.duoc.cordillera.reportservice.service.exportador.ExportadorFactory;
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
public class ReporteController {

    private final ReporteService reporteService;
    private final ExportadorFactory exportadorFactory;
    private final KpiClienteService kpiClienteService;

    @GetMapping
    public ResponseEntity<List<Reporte>> listarTodos() {
        return ResponseEntity.ok(reporteService.listarTodos());
    }

    @GetMapping("/area/{area}")
    public ResponseEntity<List<Reporte>> listarPorArea(@PathVariable String area) {
        return ResponseEntity.ok(reporteService.listarPorArea(area));
    }

    @GetMapping("/kpis")
    public ResponseEntity<List<KpiResumenDto>> listarKpis() {
        return ResponseEntity.ok(kpiClienteService.obtenerKpis());
    }

    @GetMapping("/kpis/categoria/{categoria}")
    public ResponseEntity<List<KpiResumenDto>> listarKpisPorCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(kpiClienteService.obtenerKpisPorCategoria(categoria));
    }

    @GetMapping("/{id}/exportar")
    public ResponseEntity<byte[]> exportar(
            @PathVariable Long id,
            @RequestParam(defaultValue = "pdf") String formato
    ) {
        Reporte reporte = reporteService.buscarPorId(id);
        Exportador exportador = exportadorFactory.crearExportador(formato);

        byte[] contenido = exportador.exportar(reporte);
        String nombreArchivo = "reporte-" + id + "." + exportador.getExtension();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(exportador.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .body(contenido);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reporte> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reporteService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Reporte> crear(@Valid @RequestBody Reporte reporte) {
        Reporte reporteCreado = reporteService.crear(reporte);
        return ResponseEntity.status(HttpStatus.CREATED).body(reporteCreado);
    }

    @PostMapping("/generar")
    public ResponseEntity<Reporte> generar(@RequestBody Reporte reporte) {
        Reporte reporteGenerado = reporteService.generarReporte(reporte);
        return ResponseEntity.status(HttpStatus.CREATED).body(reporteGenerado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reporte> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Reporte reporte
    ) {
        return ResponseEntity.ok(reporteService.actualizar(id, reporte));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        reporteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}