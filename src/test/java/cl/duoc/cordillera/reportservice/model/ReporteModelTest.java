package cl.duoc.cordillera.reportservice.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReporteModelTest {

  @Test
  void prePersistDebeAsignarFechaCuandoEsNula() {
    Reporte reporte = new Reporte();
    reporte.setFechaGeneracion(null);

    reporte.prePersist();

    assertNotNull(reporte.getFechaGeneracion());
  }

  @Test
  void prePersistNoDebeSobrescribirFechaExistente() {
    LocalDateTime fecha = LocalDateTime.of(2026, 5, 23, 12, 0);
    Reporte reporte = new Reporte();
    reporte.setFechaGeneracion(fecha);

    reporte.prePersist();

    assertEquals(fecha, reporte.getFechaGeneracion());
  }

  @Test
  void constructorCompletoDebeAsignarCampos() {
    LocalDateTime fecha = LocalDateTime.of(2026, 5, 23, 12, 0);

    Reporte reporte = new Reporte(
        1L,
        "Reporte Ventas",
        "EJECUTIVO",
        "Ventas",
        BigDecimal.valueOf(150000),
        fecha);

    assertEquals(1L, reporte.getId());
    assertEquals("Reporte Ventas", reporte.getTitulo());
    assertEquals("EJECUTIVO", reporte.getTipo());
    assertEquals("Ventas", reporte.getArea());
    assertEquals(BigDecimal.valueOf(150000), reporte.getValor());
    assertEquals(fecha, reporte.getFechaGeneracion());
  }
}
