package cl.duoc.cordillera.reportservice.controller;

import cl.duoc.cordillera.reportservice.model.Reporte;
import cl.duoc.cordillera.reportservice.service.ReporteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reportes")
public class ReporteController {

  private final ReporteService reporteService;

  @GetMapping
  public ResponseEntity<List<Reporte>> listarTodos() {
    return ResponseEntity.ok(reporteService.listarTodos());
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

  @PutMapping("/{id}")
  public ResponseEntity<Reporte> actualizar(
      @PathVariable Long id,
      @Valid @RequestBody Reporte reporte) {
    return ResponseEntity.ok(reporteService.actualizar(id, reporte));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> eliminar(@PathVariable Long id) {
    reporteService.eliminar(id);
    return ResponseEntity.noContent().build();
  }
}
